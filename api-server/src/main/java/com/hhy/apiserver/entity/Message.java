package com.hhy.apiserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "messages", indexes = {
        // Index để tối ưu tải tin nhắn theo hội thoại
        @Index(name = "idx_message_conversation", columnList = "conversation_id")
})
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 10)
    private MessageType messageType; // 'text', 'image', 'file'

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // --- Mối quan hệ ---

    // Ai là người gửi?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // Tin nhắn này thuộc hội thoại nào?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // --- Enum ---
    public enum MessageType {
        text,
        image,
        file,
        audio,
        video_call,
        voice_call
    }

    // --- Callbacks ---
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        if (messageType == null) {
            messageType = MessageType.text; // Mặc định là text
        }
    }
}