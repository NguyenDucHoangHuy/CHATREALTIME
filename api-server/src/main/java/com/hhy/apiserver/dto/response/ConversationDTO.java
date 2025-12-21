package com.hhy.apiserver.dto.response;

import com.hhy.apiserver.entity.Conversation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationDTO {

    private Long conversationId;
    private Conversation.ConversationType type; // 'one_to_one' hoặc 'group'

    // Thông tin hiển thị (Đã được xử lý logic trong Service)
    // - Nếu là Group: Hiển thị tên nhóm + avatar nhóm
    // - Nếu là 1-1: Hiển thị tên người kia + avatar người kia
    private String displayName;
    private String displayAvatarUrl;

    // Thông tin tin nhắn cuối cùng (để hiển thị preview trong danh sách Inbox)
    // Ví dụ: "Hôm nay ăn gì?" - 10:30 AM
    private String lastMessageContent;
    private Date lastMessageTimestamp;
    private Long lastMessageSenderId; // Để biết ai nhắn tin cuối cùng (Hiển thị "Bạn: ..." hoặc tên người gửi)
    private String lastMessageType;
    private String lastMessageSenderName;

    // ✅ THÊM 2 TRƯỜNG NÀY
    private String onlineStatus; // "online" hoặc "offline" 
    private Date lastSeen;       // Thời điểm offline gần nhất

    // Danh sách thành viên (Chỉ cần thiết khi xem chi tiết, ở list bên ngoài có thể để null)
    private List<ParticipantDTO> participants;

    private Long otherUserId; // ID người mình đang chat cùng (nếu là 1-1)
}