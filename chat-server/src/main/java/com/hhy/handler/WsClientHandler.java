package com.hhy.handler;

import com.google.gson.Gson;
import com.hhy.database.MessageDAO;
import com.hhy.manager.ClientManager;
import com.hhy.model.SocketMessage;
import com.hhy.utils.JwtHelper;
import com.hhy.utils.WebSocketUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WsClientHandler implements Runnable {

    private final Socket socket;
    private final MessageDAO messageDAO;
    private final Gson gson;

    private InputStream in;
    private OutputStream out;
    private Long currentUserId;
    private boolean isHandshakeDone = false;

    public WsClientHandler(Socket socket) {
        this.socket = socket;
        this.messageDAO = new MessageDAO();
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();

            // Vòng lặp chính đọc dữ liệu
            while (true) {
                if (!isHandshakeDone) {
                    // Giai đoạn 1: HTTP Handshake
                    if (!doHandshake()) {
                        break; // Handshake thất bại -> Ngắt
                    }
                } else {
                    // Giai đoạn 2: WebSocket Frames
                    if (!readFrame()) {
                        break; // Đọc lỗi hoặc Client đóng kết nối -> Ngắt
                    }
                }
            }
        } catch (IOException e) {
            // System.err.println("Client ngắt kết nối: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    /**
     * Xử lý bắt tay HTTP Upgrade + JWT Validation
     */
    private boolean doHandshake() throws IOException {
        // Đọc Header HTTP (đọc từng byte cho đến khi gặp \r\n\r\n)
        // Lưu ý: Đọc đơn giản cho demo, thực tế cần buffer thông minh hơn
        byte[] buffer = new byte[2048];
        int bytesRead = in.read(buffer);
        if (bytesRead == -1) return false;

        String request = new String(buffer, 0, bytesRead);

        // 1. Lấy Sec-WebSocket-Key
        Matcher keyMatch = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(request);
        if (!keyMatch.find()) return false;
        String clientKey = keyMatch.group(1).trim();

        // 2. Lấy Token từ URL (GET /?token=...)
        // Regex tìm chuỗi "token=" sau đó lấy các ký tự không phải khoảng trắng
        Matcher tokenMatch = Pattern.compile("token=([^\\s&]+)").matcher(request);
        String token = null;
        if (tokenMatch.find()) {
            token = tokenMatch.group(1);
        }

        // 3. Validate Token
        if (token == null) {
            System.out.println("❌ Kết nối bị từ chối: Không có token");
            return false;
        }

        String username = JwtHelper.extractUsername(token);
        if (username == null) {
            System.out.println("❌ Kết nối bị từ chối: Token không hợp lệ");
            return false;
        }

        // 4. Lấy User ID và Đăng nhập
        Long userId = messageDAO.getUserIdByUsername(username);
        if (userId == null) return false;

        this.currentUserId = userId;
        ClientManager.addClient(userId, this);
        messageDAO.updateUserStatus(userId, true); // Set Online

        // 5. Trả về HTTP 101 Switching Protocols
        String acceptKey = WebSocketUtil.generateAcceptKey(clientKey);
        String response = "HTTP/1.1 101 Switching Protocols\r\n" +
                "Connection: Upgrade\r\n" +
                "Upgrade: websocket\r\n" +
                "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";

        out.write(response.getBytes());
        out.flush();

        isHandshakeDone = true;
        System.out.println("✅ Handshake OK. User: " + username + " (ID: " + userId + ")");
        return true;
    }

    /**
     * Đọc và giải mã WebSocket Frame (Thủ công từng byte)
     */
    private boolean readFrame() throws IOException {
        // Đọc Byte 1: [FIN, RSV, Opcode]
        int b1 = in.read();
        if (b1 == -1) return false;

        byte opcode = (byte) (b1 & 0x0F); // Lấy 4 bit cuối

        if (opcode == 0x8) { // Opcode 8 = Close Frame
            return false;
        }

        if (opcode == 0x9) { // Opcode 9 = Ping Frame
            // Tự động trả lời Pong (Opcode 0xA) - Để sau
            return true;
        }

        // Đọc Byte 2: [Mask Bit, Payload Length]
        int b2 = in.read();
        if (b2 == -1) return false;

        boolean masked = (b2 & 0x80) != 0; // Bit đầu tiên
        long payloadLen = b2 & 0x7F;       // 7 bit sau

        // Xử lý độ dài mở rộng
        if (payloadLen == 126) {
            // Đọc tiếp 2 byte
            byte[] extended = new byte[2];
            in.read(extended);
            payloadLen = ((extended[0] & 0xFF) << 8) | (extended[1] & 0xFF);
        } else if (payloadLen == 127) {
            // Đọc tiếp 8 byte (Bỏ qua cho demo vì quá dài)
            in.skip(8);
            return true;
        }

        // Đọc Mask Key (4 byte) - Bắt buộc Client gửi lên phải có Mask
        byte[] maskingKey = new byte[4];
        if (masked) {
            in.read(maskingKey);
        }

        // Đọc Payload Data
        byte[] payload = new byte[(int) payloadLen];
        int totalRead = 0;
        while (totalRead < payloadLen) {
            int count = in.read(payload, totalRead, (int) payloadLen - totalRead);
            if (count == -1) return false;
            totalRead += count;
        }

        // Giải mã (Unmasking): Byte[i] = Encoded[i] XOR Mask[i % 4]
        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (payload[i] ^ maskingKey[i % 4]);
            }
        }

        // Chuyển thành String JSON
        String jsonMessage = new String(payload, StandardCharsets.UTF_8);
        System.out.println("📩 Nhận từ " + currentUserId + ": " + jsonMessage);

        // Xử lý nghiệp vụ
        try {
            SocketMessage msg = gson.fromJson(jsonMessage, SocketMessage.class);
            processMessage(msg);
        } catch (Exception e) {
            System.err.println("JSON lỗi: " + e.getMessage());
        }

        return true;
    }

    private void processMessage(SocketMessage msg) {
        // Logic gửi tin nhắn giữ nguyên như cũ
        if (msg.getType() == SocketMessage.ActionType.SEND_CHAT) {
            handleSendChat(msg);
        } else if (msg.getType() == SocketMessage.ActionType.MARK_READ) {
            handleMarkRead(msg);
        }
    }

    // Logic nghiệp vụ gửi tin (tái sử dụng code cũ)
    private void handleSendChat(SocketMessage msg) {
        Long conversationId = msg.getData().getConversationId();
        String content = msg.getData().getContent();
        String type = msg.getData().getMessageType();

        Long messageId = messageDAO.saveMessage(this.currentUserId, conversationId, content, type);
        if (messageId != null) {

            List<Long> members = messageDAO.getConversationMembers(conversationId);
            MessageDAO.UserBasicInfo senderInfo = messageDAO.getUserInfo(this.currentUserId);

            // Phản hồi lại cấu trúc JSON
            SocketMessage response = new SocketMessage();
            response.setType(SocketMessage.ActionType.SEND_CHAT);
            SocketMessage.MessagePayload payload = new SocketMessage.MessagePayload();
            payload.setConversationId(conversationId);
            payload.setMessageId(messageId);
            payload.setContent(content);
            payload.setMessageType(type);

            payload.setSenderId(this.currentUserId);
            payload.setSenderName(senderInfo.name);
            payload.setSenderAvatar(senderInfo.avatar);


            response.setData(payload);

            String jsonResp = gson.toJson(response);

            for (Long memberId : members) {
                // Gửi cho người khác VÀ chính mình
                WsClientHandler client = (WsClientHandler) ClientManager.getClient(memberId);
                if (client != null) {
                    client.sendFrame(jsonResp);
                }
            }
        }
    }

    private void handleMarkRead(SocketMessage msg) {
        // (Giữ nguyên logic cũ)
        Long conversationId = msg.getData().getConversationId();
        Long messageId = msg.getData().getMessageId();
        messageDAO.updateLastReadMessage(this.currentUserId, conversationId, messageId);
    }

    // Gửi Frame xuống Client (Encode trước khi gửi)
    public void sendFrame(String json) {
        try {
            byte[] frame = WebSocketUtil.encode(json);
            synchronized (out) { // Đồng bộ hóa để tránh tranh chấp luồng
                out.write(frame);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (currentUserId != null) {
            ClientManager.removeClient(currentUserId);
            messageDAO.updateUserStatus(currentUserId, false); // Set Offline + Last Seen
            System.out.println("User " + currentUserId + " disconnected.");
        }
        try { socket.close(); } catch (IOException e) {}
    }
}