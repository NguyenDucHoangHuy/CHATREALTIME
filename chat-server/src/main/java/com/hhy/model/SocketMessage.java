package com.hhy.model;

import lombok.Data;

@Data
public class SocketMessage {

    // Loại hành động
    private ActionType type;

    // Token xác thực (Chỉ cần gửi lúc đầu hoặc gửi kèm nếu muốn chắc chắn)
    private String token;

    // Dữ liệu chi tiết (Dạng JSON String hoặc Object lồng nhau)
    // Để đơn giản cho Gson, ta dùng class inner hoặc Map, ở đây tôi tách ra field cụ thể
    private MessagePayload data;

    public enum ActionType {
        LOGIN,          // Gửi token để đăng nhập
        SEND_CHAT,      // Gửi tin nhắn văn bản/ảnh
        MARK_READ,      // Đánh dấu đã đọc
        DISCONNECT      // (Tùy chọn) Báo chủ động ngắt kết nối
    }

    @Data
    public static class MessagePayload {
        private Long conversationId;
        private String content;        // Nội dung tin nhắn (hoặc URL ảnh)
        private String messageType;    // TEXT, IMAGE, FILE
        private Long messageId;        // Dùng cho hành động MARK_READ
        private Long senderId;
        private String senderName;
        private String senderAvatar;
    }
}
