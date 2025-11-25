package com.hhy;

import com.hhy.database.DatabaseConnection;
import com.hhy.handler.WsClientHandler;
import com.hhy.utils.JwtHelper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServerApplication {

    private static final int PORT = 9090;

    public static void main(String[] args) {
        System.out.println("🚀 Đang khởi động WebSocket Chat Server tại cổng " + PORT + "...");

        // 1. Kiểm tra kết nối DB
        try {
            if (DatabaseConnection.getConnection() != null) {
                System.out.println("✅ Database Connection: OK");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi Database: " + e.getMessage());
            System.err.println("-> Vui lòng kiểm tra Docker MySQL.");
            return; // Dừng server nếu không có DB
        }

        System.out.println("SECRET IN CHAT SERVER = " + JwtHelper.SECRET_KEY);

        // 2. Tạo Thread Pool (Quản lý luồng thông minh)
        // newCachedThreadPool: Tự động tạo luồng mới khi cần và tái sử dụng lại khi rảnh.
        ExecutorService threadPool = Executors.newCachedThreadPool();

        // 3. Mở ServerSocket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ WebSocket Server đang lắng nghe tại ws://localhost:" + PORT);
            System.out.println("⏳ Đang chờ Clients kết nối...");

            while (true) {
                // Chấp nhận kết nối TCP (Block)
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 Có kết nối TCP mới từ: " + clientSocket.getInetAddress());

                // Tạo Handler xử lý WebSocket Handshake & Frames
                WsClientHandler handler = new WsClientHandler(clientSocket);

                // Giao cho Thread Pool xử lý thay vì new Thread() thủ công
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Server lỗi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đóng pool khi server tắt (thường ít khi chạy tới đây trong while true)
            threadPool.shutdown();
        }
    }
}