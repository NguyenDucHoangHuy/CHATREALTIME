package com.hhy.apiserver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Getter // Thay @Data bằng @Getter
@Setter // Thay @Data bằng @Setter
@NoArgsConstructor
// Loại bỏ các trường quan hệ khỏi equals/hashCode để tránh vòng lặp
@EqualsAndHashCode(exclude = {"participants", "sentMessages", "refreshTokens"})
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "user_name", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_phone", columnList = "phone_number", unique = true)
})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "online_status", length = 10)
    private OnlineStatus onlineStatus;

    @Column(name = "last_seen")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSeen;  //Để hiển thị “Online 5 phút trước”.

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    // --- Enums ---
    public enum Gender {
        male, female, other
    }

    public enum OnlineStatus {
        online, offline
    }

    // --- Callbacks ---
    @PrePersist // hàm thực thi trước khi entity được persist
    protected void onCreate() {
        createdAt = new Date();
        lastSeen = new Date();

        // LOGIC MỚI: Chỉ set offline nếu chưa ai set giá trị (null)
        if (onlineStatus == null) {
            onlineStatus = OnlineStatus.offline;
        }
    }

    // --- Mối quan hệ ---

    @OneToMany(mappedBy = "user")
    private Set<Participant> participants; // Các cuộc hội thoại mà user này tham gia

    @OneToMany(mappedBy = "sender")
    private Set<Message> sentMessages; // Các tin nhắn user này gửi

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL, // Quan trọng
            orphanRemoval = true       // Quan trọng
    )
    private Set<RefreshToken> refreshTokens;

    // (Quan hệ Friendship phức tạp hơn, ta sẽ truy vấn riêng thay vì map ở đây)

    // 3. THÊM CÁC HÀM CỦA "UserDetails"
    // (Lombok @Data đã tự tạo getUsername() và getPassword())

    @Override
    public String getPassword() {
        return passwordHash; // Trả về cột password hash
    }

    @Override
    public String getUsername() {
        return username; // Trả về cột username (hoặc email, SĐT tùy bạn)
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Đồ án này không dùng Role (quyền),
        // nên ta trả về một danh sách rỗng.
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Tài khoản không bao giờ hết hạn
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Tài khoản không bao giờ bị khóa
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Thông tin (mật khẩu) không bao giờ hết hạn
    }

    @Override
    public boolean isEnabled() {
        return true; // Tài khoản luôn được kích hoạt
    }
}