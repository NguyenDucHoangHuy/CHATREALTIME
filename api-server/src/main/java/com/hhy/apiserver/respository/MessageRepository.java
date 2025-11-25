package com.hhy.apiserver.respository;


import com.hhy.apiserver.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 1. Dùng để tải lịch sử chat của một cuộc hội thoại
    // Chúng ta dùng Pageable để triển khai "tải thêm" (infinite scrolling)
    // Sắp xếp theo thời gian mới nhất (DESC)
    Page<Message> findByConversationConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);
}