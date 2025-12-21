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
        DISCONNECT,      // (Tùy chọn) Báo chủ động ngắt kết nối

        // --- WebRTC Video Call (MỚI THÊM) ---
        CALL_OFFER,      // A gửi lời mời gọi (kèm SDP)
        CALL_ANSWER,     // B chấp nhận (kèm SDP)
        ICE_CANDIDATE,   // Các ứng viên mạng (đường đi)
        CALL_REJECT,     // B từ chối hoặc đang bận
        CALL_END,         // Kết thúc cuộc gọi
        CAMERA_TOGGLE    //Xử lý tắt/mở camera
    }

    @Data
    public static class MessagePayload {
        private Long conversationId;
        private String content;        // Nội dung tin nhắn (hoặc URL ảnh)
        private String messageType;    // TEXT, IMAGE, FILE
        private Long messageId;        // Dùng cho hành động MARK_READ
        private Long senderId;
        private String senderUsername;
        private String senderAvatar;

        // --- WebRTC Fields (MỚI) ---
        private Long targetUserId; // ID người mình muốn gọi


        private String sdp;       // Session Description Protocol (Offer/Answer)
        private String candidate; // ICE Candidate info

        private Boolean isCameraOn;
        private String callType; // "VIDEO" hoặc "VOICE"
    }
}
