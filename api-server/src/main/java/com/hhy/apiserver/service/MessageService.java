package com.hhy.apiserver.service;

import com.hhy.apiserver.dto.request.message.MarkReadRequestDTO;
import com.hhy.apiserver.dto.response.MessageDTO;
import com.hhy.apiserver.entity.Conversation;
import com.hhy.apiserver.entity.Message;
import com.hhy.apiserver.entity.Participant;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.respository.ConversationRepository;
import com.hhy.apiserver.respository.MessageRepository;
import com.hhy.apiserver.respository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ParticipantRepository participantRepository;
    private final ConversationRepository conversationRepository;

    // 1. Tải lịch sử tin nhắn (Infinite Scroll)
    public Page<MessageDTO> getMessages(User currentUser, Long conversationId, Pageable pageable) {
        // Kiểm tra xem user có trong cuộc hội thoại này không
        if (participantRepository.findByUserIdAndConversationId(currentUser.getUserId(), conversationId).isEmpty()) {
            throw new RuntimeException("Bạn không có quyền xem cuộc trò chuyện này");
        }

        // Lấy từ DB (đã sắp xếp giảm dần theo thời gian trong Repository)
        Page<Message> messagePage = messageRepository.findByConversationConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        // Chuyển đổi sang DTO (sử dụng constructor của MessageDTO)
        return messagePage.map(MessageDTO::new);
    }

    // 2. Xóa tin nhắn
    @Transactional
    public void deleteMessage(User currentUser, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        // Logic quyền xóa: Chỉ người gửi mới được xóa
        if (!message.getSender().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Bạn chỉ có thể xóa tin nhắn của chính mình");
        }

        // Logic cập nhật LastMessage nếu tin bị xóa là tin cuối cùng (Nâng cao - Optional)
        Conversation conversation = message.getConversation();
        if (conversation.getLastMessage() != null && conversation.getLastMessage().getMessageId().equals(messageId)) {
            conversation.setLastMessage(null); // Tạm thời set null hoặc tìm tin trước đó
            conversationRepository.save(conversation);
        }

        messageRepository.delete(message);
    }

    // 3. Đánh dấu đã đọc
    @Transactional
    public void markAsRead(User currentUser, MarkReadRequestDTO request) {
        // Tìm thông tin tham gia của user trong cuộc hội thoại đó
        Participant participant = participantRepository.findByUserIdAndConversationId(currentUser.getUserId(), request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Bạn không ở trong cuộc trò chuyện này"));

        // Cập nhật ID tin nhắn đã đọc
        // Logic đơn giản: Chỉ cập nhật nếu ID mới lớn hơn ID cũ (đọc tiến chứ không đọc lùi)
        Long currentReadId = participant.getLastReadMessageId() != null ? participant.getLastReadMessageId() : 0L;

        if (request.getMessageId() > currentReadId) {
            participant.setLastReadMessageId(request.getMessageId());
            participantRepository.save(participant);
        }
    }
}