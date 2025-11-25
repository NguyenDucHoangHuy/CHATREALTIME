package com.hhy.apiserver.respository;

import com.hhy.apiserver.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Friendship.FriendshipId> {

    // 2. Dùng để lấy danh sách bạn bè (status = 'accepted') của một user
    // Phải dùng @Query vì user có thể là requester_id HOẶC addressee_id
    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = 'accepted'")
    List<Friendship> findAllFriendsByUserId(@Param("userId") Long userId);

    // 3. Dùng để kiểm tra trạng thái quan hệ giữa 2 người (đã là bạn, đã gửi,...)
    // Phải dùng @Query để kiểm tra cả 2 chiều
    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :userId1 AND f.addresseeId = :userId2) OR (f.requesterId = :userId2 AND f.addresseeId = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 1. Tìm mối quan hệ cụ thể giữa 2 người (theo chiều xuôi)
    // Dùng để check khi Accept/Decline (Requester -> Addressee)
    Optional<Friendship> findByRequesterIdAndAddresseeId(Long requesterId, Long addresseeId);

    // 2. Tìm danh sách lời mời ĐẾN (Status = PENDING, Addressee = Me)
    List<Friendship> findByAddresseeIdAndStatus(Long addresseeId, Friendship.FriendshipStatus status);

    // 3. Tìm danh sách lời mời ĐI (Status = PENDING, Requester = Me)
    List<Friendship> findByRequesterIdAndStatus(Long requesterId, Friendship.FriendshipStatus status);
}