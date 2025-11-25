package com.hhy.apiserver.respository;

import com.hhy.apiserver.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // 1. Dùng để kiểm tra xem cuộc chat 1-1 giữa 2 người đã tồn tại chưa
    // Đây là một query phức tạp, tìm conversation loại 'one_to_one'
    // mà CÓ CHỨA cả 2 userId này trong bảng participants
    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
            "WHERE c.type = 'one_to_one' " +
            "AND p1.userId = :userId1 " +
            "AND p2.userId = :userId2")
    Optional<Conversation> findExistingOneToOneConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}