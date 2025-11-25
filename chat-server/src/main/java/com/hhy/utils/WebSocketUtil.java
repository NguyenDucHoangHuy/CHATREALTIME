package com.hhy.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class WebSocketUtil {
    private static final String MAGIC_STRING = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    /**
     * 1. Xử lý Handshake
     * Input: Client Key (từ header Sec-WebSocket-Key)
     * Output: Accept Key (để trả về trong header Sec-WebSocket-Accept)
     */
    public static String generateAcceptKey(String clientKey) {
        try {
            String combined = clientKey + MAGIC_STRING;
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hashedBytes = sha1.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi thuật toán SHA-1");
        }
    }

    /**
     * 2. Giải mã Frame (Decode) - Từ Client gửi lên Server
     * Client luôn Mask dữ liệu. Server phải Unmask.
     * Việc này để handler tự làm luôn
     */


    /**
     * 3. Đóng gói Frame (Encode) - Từ Server gửi xuống Client
     * Server không cần Mask dữ liệu.
     */
    public static byte[] encode(String message) {
        byte[] rawData = message.getBytes(StandardCharsets.UTF_8);
        int len = rawData.length;

        // Tính toán kích thước frame
        int headerLen;
        if (len <= 125) headerLen = 2;
        else if (len <= 65535) headerLen = 4;
        else headerLen = 10;

        byte[] frame = new byte[headerLen + len];

        // Byte 0: FIN=1 (1000) | Text Frame=1 (0001) -> 10000001 = 0x81
        frame[0] = (byte) 0x81;

        // Byte 1: Mask=0 | Payload Len
        if (len <= 125) {
            frame[1] = (byte) len;
            System.arraycopy(rawData, 0, frame, 2, len);
        } else if (len <= 65535) {
            frame[1] = (byte) 126;
            frame[2] = (byte) ((len >> 8) & 0xFF);
            frame[3] = (byte) (len & 0xFF);
            System.arraycopy(rawData, 0, frame, 4, len);
        } else {
            // (Xử lý cho tin cực lớn - bỏ qua cho gọn)
        }

        return frame;
    }
}
