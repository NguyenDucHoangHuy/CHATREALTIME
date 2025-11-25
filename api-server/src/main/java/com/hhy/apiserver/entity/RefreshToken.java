package com.hhy.apiserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết với user, một user CÓ THỂ CÓ nhiều refresh token (nếu đăng nhập nhiều nơi)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String token; // Chuỗi token (JWT)

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate; // Thời điểm hết hạn

    @PrePersist
    protected void onCreate() {
        // Có thể thêm logic gì đó khi tạo nếu cần
    }
}