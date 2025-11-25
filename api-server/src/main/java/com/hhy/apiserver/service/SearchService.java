package com.hhy.apiserver.service;


import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.Friendship;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.mapper.UserMapper;
import com.hhy.apiserver.respository.FriendshipRepository;
import com.hhy.apiserver.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserMapper userMapper;

    /**
     * 1. Logic cho /api/search/user
     * Tìm tất cả user có username khớp, TRỪ BẢN THÂN MÌNH
     */
    public List<UserDTO> searchUsers(String query, Long currentUserId) {
        // 1. Gọi hàm mới từ Repository (chúng ta sẽ tạo ở bước 3)
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseAndUserIdNot(query, currentUserId);

        // 2. Dùng MapStruct để chuyển List<User> -> List<UserDTO>
        return users.stream()
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * 2. Logic cho /api/search/friends
     * Chỉ tìm trong danh sách bạn bè đã 'accepted'
     */
    public List<UserDTO> searchFriends(User currentUser, String query) {
        // 1. Lấy tất cả bạn bè (status = 'accepted') của user hiện tại
        // (Hàm này chúng ta đã viết trong FriendshipRepository)
        List<Friendship> friendships = friendshipRepository.findAllFriendsByUserId(currentUser.getUserId());

        // 2. Dùng Java Stream để xử lý:
        return friendships.stream()
                // 2a. Trích xuất object User "bạn bè" (người KHÔNG PHẢI là mình)
                .map(friendship -> {
                    if (friendship.getRequester().getUserId().equals(currentUser.getUserId())) {
                        return friendship.getAddressee();
                    } else {
                        return friendship.getRequester();
                    }
                })
                // 2b. Lọc danh sách bạn bè này theo 'query' (không phân biệt chữ hoa/thường)
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                // 2c. Chuyển List<User> -> List<UserDTO>
                .map(userMapper::toUserDTO)
                .collect(Collectors.toList());
    }
}

