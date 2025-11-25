package com.hhy.apiserver.dto.response;

import com.hhy.apiserver.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {

    private Long messageId;
    private String messageContent;

    // Dùng Enum trực tiếp từ Entity
    private Message.MessageType messageType;

    private Date createdAt;

    // --- Thông tin người gửi (Flatten) ---
    // Thay vì trả về cả object UserDTO, ta chỉ lấy những thứ cần thiết để hiển thị
    // giúp JSON gọn nhẹ hơn.
    private Long senderId;
    private String senderUsername;
    private String senderAvatarUrl;

    // ID của cuộc hội thoại (để client biết tin nhắn này thuộc về đâu)
    private Long conversationId;

    // --- Constructor chuyển đổi từ Entity sang DTO ---
    // Được dùng trong MessageService: .map(MessageDTO::new)
    public MessageDTO(Message message) {
        this.messageId = message.getMessageId();
        this.messageContent = message.getMessageContent();
        this.messageType = message.getMessageType();
        this.createdAt = message.getCreatedAt();

        // Map thông tin cuộc hội thoại
        if (message.getConversation() != null) {
            this.conversationId = message.getConversation().getConversationId();
        }

        // Map thông tin người gửi (Kiểm tra null để tránh lỗi)
        if (message.getSender() != null) {
            this.senderId = message.getSender().getUserId();
            this.senderUsername = message.getSender().getUsername();
            this.senderAvatarUrl = message.getSender().getAvatarUrl();
        }
    }
}