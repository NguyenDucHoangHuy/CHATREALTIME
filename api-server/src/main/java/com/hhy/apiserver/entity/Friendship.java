package com.hhy.apiserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@Entity
@Table(name = "friendships", indexes = {
        @Index(name = "idx_friendship_user", columnList = "requester_id, addressee_id"),
        @Index(name = "idx_friendship_status", columnList = "status")
})
@IdClass(Friendship.FriendshipId.class)
public class Friendship {
    @Id
    @Column(name = "requester_id")
    private Long requesterId; // User gửi yêu cầu

    @Id
    @Column(name = "addressee_id")
    private Long addresseeId; // User nhận yêu cầu

    // --- Mối quan hệ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", insertable = false, updatable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", insertable = false, updatable = false)
    private User addressee;

    // --- Các cột khác ---
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private FriendshipStatus status;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    // --- Enum ---
    public enum FriendshipStatus {
        pending,
        accepted,
        declined,
        blocked
    }

    // --- Callbacks ---
    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // --- Lớp IdClass cho khóa chính 2 cột ---
    @EqualsAndHashCode
    public static class FriendshipId implements Serializable {
        private Long requesterId;
        private Long addresseeId;
        // Cần equals và hashCode (Lombok @Data/@EqualsAndHashCode sẽ tự lo nếu bạn tạo file riêng)
    }
}
