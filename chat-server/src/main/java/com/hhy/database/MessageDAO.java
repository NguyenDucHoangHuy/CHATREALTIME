package com.hhy.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // 1. Tìm userId từ username (Lấy từ token)
    public Long getUserIdByUsername(String username) {
        String sql = "SELECT user_id FROM users WHERE user_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("user_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. Cập nhật trạng thái Online/Offline & Last Seen
    public void updateUserStatus(Long userId, boolean isOnline) {
        String status = isOnline ? "online" : "offline";
        // Nếu offline thì cập nhật cả last_seen = NOW()
        String sql = isOnline
                ? "UPDATE users SET online_status = ? WHERE user_id = ?"
                : "UPDATE users SET online_status = ?, last_seen = NOW() WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Lưu tin nhắn mới & Cập nhật Conversation
    public Long saveMessage(Long senderId, Long conversationId, String content, String type) {
        String sqlInsert = "INSERT INTO messages (conversation_id, sender_id, message_content, message_type, created_at) VALUES (?, ?, ?, ?, NOW())";
        String sqlUpdateConv = "UPDATE conversations SET last_message_id = ? WHERE conversation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaction

            // A. Insert Message
            Long messageId = null;
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, conversationId);
                stmt.setLong(2, senderId);
                stmt.setString(3, content);
                stmt.setString(4, type);
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) messageId = rs.getLong(1);
            }

            // B. Update Conversation (last_message_id)
            if (messageId != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlUpdateConv)) {
                    stmt.setLong(1, messageId);
                    stmt.setLong(2, conversationId);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return messageId;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. Lấy danh sách ID thành viên trong nhóm (để Broadcast)
    public List<Long> getConversationMembers(Long conversationId) {
        List<Long> members = new ArrayList<>();
        String sql = "SELECT user_id FROM participants WHERE conversation_id = ? AND status = 'active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(rs.getLong("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    // 5. Cập nhật Last Read Message (Đã xem)
    public void updateLastReadMessage(Long userId, Long conversationId, Long messageId) {
        // Chỉ cập nhật nếu messageId mới lớn hơn cái cũ
        String sql = "UPDATE participants SET last_read_message_id = ? " +
                "WHERE user_id = ? AND conversation_id = ? AND (last_read_message_id IS NULL OR last_read_message_id < ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            stmt.setLong(2, userId);
            stmt.setLong(3, conversationId);
            stmt.setLong(4, messageId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Hàm lấy info người gửi (Tên, Avatar)
    public UserBasicInfo getUserInfo(Long userId) {
        String sql = "SELECT user_name, avatar_url FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserBasicInfo(rs.getString("user_name"), rs.getString("avatar_url"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new UserBasicInfo("Unknown", null);
    }
    // Class con để chứa dữ liệu tạm
    public static class UserBasicInfo {
        public String name;
        public String avatar;

        public UserBasicInfo(String name, String avatar) {
            this.name = name;
            this.avatar = avatar;
        }
    }
}
