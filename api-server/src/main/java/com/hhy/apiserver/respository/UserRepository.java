package com.hhy.apiserver.respository;

import com.hhy.apiserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Dùng cho Đăng nhập (hoặc tìm kiếm) bằng email
    Optional<User> findByEmail(String email);

    // 2. Dùng cho Đăng nhập (hoặc tìm kiếm) bằng SĐT
    Optional<User> findByPhoneNumber(String phoneNumber);

    // 3. Dùng để kiểm tra nhanh khi đăng ký (hiệu quả hơn .find...().isPresent())
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    // 1. Dùng cho Đăng nhập (hoặc tìm kiếm) bằng email
    Optional<User> findByUsername(String username);


    // 4. Dùng cho chức năng tìm kiếm bạn bè (tìm gần đúng)
    // Ví dụ: tìm "nhat02" sẽ ra "quocnhat02"
    Optional<User> findByUsernameContaining(String username);


    /**
     * Tìm kiếm user theo username (giống 'LIKE %query%')
     * KHÔNG phân biệt chữ hoa/thường (IgnoreCase)
     * VÀ KHÔNG bao gồm chính user hiện tại (UserIdNot)
     */
    List<User> findByUsernameContainingIgnoreCaseAndUserIdNot(String username, Long userId);

}
