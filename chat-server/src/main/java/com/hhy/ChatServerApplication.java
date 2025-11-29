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











//✅ 2. newCachedThreadPool() là gì?
//
//Nó là Thread Pool có đặc điểm:
//
//        ✔ Không giới hạn số lượng thread (unbounded pool)
//
//Khi có client mới → nó tạo thread mới ngay lập tức.
//
//✔ Nếu một thread rảnh > 60 giây → nó bị shutdown
//
//→ Dọn dẹp thread không còn dùng
//→ Giảm tốn RAM
//
//✔ Tái sử dụng lại thread cũ nếu có thể
//
//→ Không phải tạo thread mới (tạo thread tốn tài nguyên)
//
//✔ Rất phù hợp cho ứng dụng có số lượng kết nối không ổn định
//
//WebSocket chat server của bạn thường:
//
//Có lúc nhiều users vào
//
//Có lúc ít
//
//Kết nối sống lâu nhưng đôi khi mất