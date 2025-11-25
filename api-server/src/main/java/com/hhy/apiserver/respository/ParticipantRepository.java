package com.hhy.apiserver.respository;

import com.hhy.apiserver.entity.Conversation;
import com.hhy.apiserver.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Participant.ParticipantId> {

    // 1. Dùng để lấy participant (để kiểm tra status, role)
    Optional<Participant> findByUserIdAndConversationId(Long userId, Long conversationId);

    // 2. Dùng để TẢI INBOX CHÍNH
    // Lấy tất cả Conversations mà tôi tham gia VÀ có status = 'active'
    // Sắp xếp theo tin nhắn cuối cùng (nếu đã implement last_message_id)
    @Query("SELECT p.conversation FROM Participant p " +
            "WHERE p.userId = :userId AND p.status = 'active' " +
            "ORDER BY p.conversation.lastMessage.createdAt DESC")
    List<Conversation> findActiveConversationsByUserId(@Param("userId") Long userId);

    // 3. Dùng để TẢI MỤC "TIN NHẮN ĐANG CHỜ"
    // Lấy tất cả Conversations mà tôi tham gia VÀ có status = 'pending'
    @Query("SELECT p.conversation FROM Participant p " +
            "WHERE p.userId = :userId AND p.status = 'pending' " +
            "ORDER BY p.conversation.lastMessage.createdAt DESC")
    List<Conversation> findPendingConversationsByUserId(@Param("userId") Long userId);
}