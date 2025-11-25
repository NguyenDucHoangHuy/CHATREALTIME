package com.hhy.apiserver.respository;


import com.hhy.apiserver.entity.RefreshToken;
import com.hhy.apiserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 1. Dùng để tìm token khi user yêu cầu "refresh"
    // Đây là hàm quan trọng nhất
    Optional<RefreshToken> findByToken(String token);

    // 2. Dùng khi user đăng xuất, hoặc khi token bị "thu hồi" (revoke)
    // Chúng ta sẽ xóa token này khỏi CSDL
    void deleteByUser(User user);

    // (Tùy chọn) Tìm token của một user cụ thể nếu bạn muốn quản lý
    // các phiên đăng nhập
    Optional<RefreshToken> findByUserUserId(Long userId);
}