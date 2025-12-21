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



    // Interface để hứng kết quả query native
    interface UserSuggestionProjection {
        Long getUserId();
        String getUsername();
        String getAvatarUrl();
        Integer getMutualFriendsCount();
    }

    /**
     * Logic Query:
     * 1. Tìm tất cả bạn bè của tôi (My Friends).
     * 2. Tìm tất cả bạn bè của (My Friends) -> Gọi là Candidates.
     * 3. Loại bỏ chính tôi ra khỏi Candidates.
     * 4. Loại bỏ những người đã là bạn hoặc đã có request pending với tôi.
     * 5. Group by Candidate và đếm số lần xuất hiện (chính là số bạn chung).
     * 6. Sắp xếp giảm dần và limit 7.
     */
    @Query(value = """
        SELECT 
            u.user_id AS userId, 
            u.user_name AS username, 
            u.avatar_url AS avatarUrl, 
            COUNT(DISTINCT mutual_friend.user_id) AS mutualFriendsCount
        FROM users u
        
        -- Join để tìm bạn của Candidate (Candidate <-> Mutual Friend)
        JOIN friendships f1 ON (f1.requester_id = u.user_id OR f1.addressee_id = u.user_id)
        JOIN users mutual_friend ON (CASE WHEN f1.requester_id = u.user_id THEN f1.addressee_id ELSE f1.requester_id END = mutual_friend.user_id)
        
        -- Join để kiểm tra Mutual Friend có phải bạn của Tôi không (Mutual Friend <-> Me)
        JOIN friendships f2 ON (f2.requester_id = mutual_friend.user_id OR f2.addressee_id = mutual_friend.user_id)
        
        WHERE f1.status = 'accepted' 
          AND f2.status = 'accepted'
          AND (f2.requester_id = :myId OR f2.addressee_id = :myId) -- f2 phải dính tới tôi
          AND u.user_id != :myId -- Candidate không phải là tôi
          
          -- Quan trọng: Candidate KHÔNG được có bất kỳ quan hệ nào với tôi (pending/accepted/blocked)
          AND NOT EXISTS (
              SELECT 1 FROM friendships check_f 
              WHERE (check_f.requester_id = :myId AND check_f.addressee_id = u.user_id)
                 OR (check_f.addressee_id = :myId AND check_f.requester_id = u.user_id)
          )
          
        GROUP BY u.user_id, u.user_name, u.avatar_url
        ORDER BY mutualFriendsCount DESC
        LIMIT 7
        """, nativeQuery = true)
    List<UserSuggestionProjection> findFriendSuggestions(@Param("myId") Long myId);
}