package com.hhy.apiserver.service;

import com.hhy.apiserver.dto.request.friendship.FriendIdRequestDTO;
import com.hhy.apiserver.dto.response.FriendRequestDTO;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.Friendship;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.exception.AppException;
import com.hhy.apiserver.exception.ErrorCode;
import com.hhy.apiserver.mapper.UserMapper;
import com.hhy.apiserver.respository.FriendshipRepository;
import com.hhy.apiserver.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // 1. Gửi lời mời kết bạn
    @Transactional
    public void sendFriendRequest(User currentUser, FriendIdRequestDTO request) {
        Long targetId = request.getTargetUserId();

        if (currentUser.getUserId().equals(targetId)) {
            throw new RuntimeException("Không thể kết bạn với chính mình");
        }

        // Kiểm tra xem targetUser có tồn tại không
        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra xem đã có quan hệ chưa (bất kể chiều nào)
        // Logic đơn giản: Thử tìm cả 2 chiều, nếu thấy thì báo lỗi
        if (existsFriendship(currentUser.getUserId(), targetId)) {
            throw new RuntimeException("Đã tồn tại mối quan hệ hoặc lời mời");
        }

        // Tạo Friendship mới
        Friendship friendship = new Friendship();
        friendship.setRequesterId(currentUser.getUserId());
        friendship.setAddresseeId(targetId);
        friendship.setStatus(Friendship.FriendshipStatus.pending);

        // Vì ta dùng @IdClass nên cần set cả Object reference nếu dùng JPA thuần,
        // nhưng với cách thiết kế này set ID là đủ hoặc set Object tùy config.
        // Để an toàn nhất với JPA:
        friendship.setRequester(currentUser);
        friendship.setAddressee(targetUser);

        friendshipRepository.save(friendship);
    }

    // 2. Chấp nhận lời mời
    @Transactional
    public void acceptFriendRequest(User currentUser, FriendIdRequestDTO request) {
        // Người gửi yêu cầu là targetUserId (người kia), Người nhận là currentUser (mình)
        Friendship friendship = friendshipRepository.findByRequesterIdAndAddresseeId(request.getTargetUserId(), currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời kết bạn"));

        if (friendship.getStatus() != Friendship.FriendshipStatus.pending) {
            throw new RuntimeException("Lời mời không hợp lệ");
        }

        friendship.setStatus(Friendship.FriendshipStatus.accepted);
        friendshipRepository.save(friendship);
    }

    // 3. Từ chối lời mời (Xóa luôn bản ghi để họ có thể gửi lại sau này nếu muốn)
    @Transactional
    public void declineFriendRequest(User currentUser, FriendIdRequestDTO request) {
        Friendship friendship = friendshipRepository.findByRequesterIdAndAddresseeId(request.getTargetUserId(), currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời kết bạn"));

        friendshipRepository.delete(friendship);
    }

    // 4. Hủy lời mời đã gửi
    @Transactional
    public void cancelFriendRequest(User currentUser, FriendIdRequestDTO request) {
        // Mình là requester, họ là addressee
        Friendship friendship = friendshipRepository.findByRequesterIdAndAddresseeId(currentUser.getUserId(), request.getTargetUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời đã gửi"));

        friendshipRepository.delete(friendship);
    }


    // 5. Hủy kết bạn (Unfriend)
    @Transactional
    public void removeFriend(User currentUser, Long friendId) {
        // Dùng hàm 2 chiều để tìm mối quan hệ bất kể ai gửi trước
        Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(currentUser.getUserId(), friendId)
                .orElseThrow(() -> new RuntimeException("Hai người chưa kết bạn")); // Bạn có thể thêm ErrorCode này

        friendshipRepository.delete(friendship);
    }

    // 6. Lấy danh sách bạn bè
    public List<UserDTO> getFriendList(User currentUser) {
        // Sử dụng hàm @Query đã viết trong Repository để lấy tất cả Friendship 'accepted'
        List<Friendship> friendships = friendshipRepository.findAllFriendsByUserId(currentUser.getUserId());

        return friendships.stream()
                .map(f -> {
                    // Logic để lấy ra người "kia" (không phải mình)
                    User friend = f.getRequesterId().equals(currentUser.getUserId())
                            ? f.getAddressee() : f.getRequester();
                    return userMapper.toUserDTO(friend);
                })
                .collect(Collectors.toList());
    }

    // 7. Lấy danh sách lời mời ĐẾN (Incoming)
    public List<FriendRequestDTO> getIncomingRequests(User currentUser) {
        List<Friendship> requests = friendshipRepository.findByAddresseeIdAndStatus(
                currentUser.getUserId(), Friendship.FriendshipStatus.pending);

        return requests.stream()
                .map(f -> {
                    FriendRequestDTO dto = new FriendRequestDTO();
                    dto.setRequesterInfo(userMapper.toUserDTO(f.getRequester()));
                    dto.setRequestDate(f.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 8. Lấy danh sách lời mời ĐI (Outgoing)
    public List<UserDTO> getOutgoingRequests(User currentUser) {
        List<Friendship> requests = friendshipRepository.findByRequesterIdAndStatus(
                currentUser.getUserId(), Friendship.FriendshipStatus.pending);

        return requests.stream()
                .map(f -> userMapper.toUserDTO(f.getAddressee()))
                .collect(Collectors.toList());
    }

    // Helper: Kiểm tra tồn tại (Dùng hàm 2 chiều -> Gọn hơn nhiều)
    private boolean existsFriendship(Long id1, Long id2) {
        return friendshipRepository.findFriendshipBetweenUsers(id1, id2).isPresent();
    }
}