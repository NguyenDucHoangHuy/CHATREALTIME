package com.hhy.apiserver.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EqualsAndHashCode(exclude = {"participants", "messages", "lastMessage"})
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 15)
    private ConversationType type; // 'one_to_one' hoặc 'group'

    @Column(name = "group_name", length = 150)
    private String groupName; // Tên nhóm (nếu là group)

    @Column(name = "group_avatar_url", columnDefinition = "TEXT")
    private String groupAvatarUrl; // Avatar nhóm (nếu là group)

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // --- Mối quan hệ ---

    // Tham chiếu đến tin nhắn cuối cùng (để tối ưu)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private Message lastMessage;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private Set<Participant> participants; // Các thành viên trong hội thoại

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private Set<Message> messages; // Các tin nhắn trong hội thoại

    // --- Enum ---
    public enum ConversationType {
        one_to_one,
        group
    }

    // --- Callbacks ---
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
