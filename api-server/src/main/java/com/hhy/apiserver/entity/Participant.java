package com.hhy.apiserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"user", "conversation"}) // Loại bỏ User và Conversation
@Table(name = "participants")
// Dùng IdClass cho khóa chính 2 cột
@IdClass(Participant.ParticipantId.class)
public class Participant {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "conversation_id")
    private Long conversationId;

    // --- Mối quan hệ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", insertable = false, updatable = false)
    private Conversation conversation;

    // --- Các cột khác ---
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10)
    private Role role; // 'admin' hoặc 'member'

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private ConversationStatus status; // 'pending' hoặc 'active'

    @Column(name = "joined_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date joinedAt;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId; // ID của tin nhắn mới nhất mà user đã xem

    // --- Enums ---
    public enum Role {
        admin, member
    }

    public enum ConversationStatus {
        pending, // Tin nhắn đang chờ
        active   // Đã chấp nhận (inbox chính)
    }

    // --- Callbacks ---
    @PrePersist
    protected void onCreate() {
        joinedAt = new Date();
        if (role == null) {
            role = Role.member; // Mặc định là member
        }
    }

    // --- Lớp IdClass cho khóa chính 2 cột ---
    @EqualsAndHashCode
    public static class ParticipantId implements Serializable {
        private Long userId;
        private Long conversationId;
    }
}