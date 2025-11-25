package com.hhy.manager;


import com.hhy.handler.WsClientHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {

    // Map: UserId -> WsClientHandler (Chứa kết nối WebSocket)
    private static final ConcurrentHashMap<Long, WsClientHandler> clients = new ConcurrentHashMap<>();

    // Đăng ký user khi họ Handshake & Login thành công
    public static void addClient(Long userId, WsClientHandler handler) {
        clients.put(userId, handler);
        System.out.println("✅ User " + userId + " đã online (WebSocket Registered).");
    }

    // Xóa user khi họ ngắt kết nối
    public static void removeClient(Long userId) {
        if (userId != null && clients.containsKey(userId)) {
            clients.remove(userId);
            System.out.println("❌ User " + userId + " đã offline.");
        }
    }

    // Lấy handler của user để gửi tin nhắn (Send Frame)
    public static WsClientHandler getClient(Long userId) {
        return clients.get(userId);
    }

    // Kiểm tra user có online không
    public static boolean isUserOnline(Long userId) {
        return clients.containsKey(userId);
    }

    // (Optional) Hàm Broadcast cho toàn bộ server (Ví dụ thông báo bảo trì)
    public static void broadcast(String jsonMessage) {
        for (WsClientHandler client : clients.values()) {
            client.sendFrame(jsonMessage);
        }
    }
}
