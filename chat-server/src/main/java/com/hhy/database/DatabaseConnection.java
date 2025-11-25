package com.hhy.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Cấu hình DB (Khớp với docker-compose và api-server)
    private static final String DB_URL = "jdbc:mysql://localhost:3307/chatapp";
    private static final String USER = "root";
    private static final String PASS = "root";

    static {
        try {
            // Load driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy Driver MySQL!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Hàm lấy kết nối
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // Test thử kết nối ngay khi chạy file này
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Kết nối Database thành công!");
        } catch (SQLException e) {
            System.err.println("❌ Kết nối thất bại: " + e.getMessage());
        }
    }
}
