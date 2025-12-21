package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.request.friendship.FriendIdRequestDTO;
import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.dto.response.FriendRequestDTO;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.dto.response.UserSuggestionDTO;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // 1. Gửi lời mời
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<String>> sendRequest(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendIdRequestDTO request) {
        friendshipService.sendFriendRequest(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã gửi lời mời kết bạn").build());
    }

    // 2. Chấp nhận
    @PostMapping("/accept")
    public ResponseEntity<ApiResponse<String>> acceptRequest(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendIdRequestDTO request) {
        friendshipService.acceptFriendRequest(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã chấp nhận kết bạn").build());
    }

    // 3. Từ chối
    @PostMapping("/decline")
    public ResponseEntity<ApiResponse<String>> declineRequest(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendIdRequestDTO request) {
        friendshipService.declineFriendRequest(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã từ chối lời mời").build());
    }

    // 4. Hủy lời mời đã gửi
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> cancelRequest(
            @AuthenticationPrincipal User currentUser,
            @RequestBody FriendIdRequestDTO request) {
        friendshipService.cancelFriendRequest(currentUser, request);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã hủy lời mời").build());
    }

    // 5. Hủy kết bạn (Unfriend)
    @DeleteMapping("/remove/{friendId}")
    public ResponseEntity<ApiResponse<String>> removeFriend(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long friendId) {
        friendshipService.removeFriend(currentUser, friendId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã hủy kết bạn").build());
    }

    // 6. Danh sách bạn bè
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getFriendList(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.<List<UserDTO>>builder()
                .code(1000)
                .message("Lấy danh sách bạn bè thành công")
                .result(friendshipService.getFriendList(currentUser))
                .build());
    }

    // 7. Danh sách lời mời ĐẾN (Người khác gửi cho mình)
    @GetMapping("/requests/incoming")
    public ResponseEntity<ApiResponse<List<FriendRequestDTO>>> getIncomingRequests(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.<List<FriendRequestDTO>>builder()
                .code(1000)
                .message("Lấy danh sách lời mời đến thành công")
                .result(friendshipService.getIncomingRequests(currentUser))
                .build());
    }

    // 8. Danh sách lời mời ĐI (Mình gửi cho người khác)
    @GetMapping("/requests/outgoing")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getOutgoingRequests(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.<List<UserDTO>>builder()
                .code(1000)
                .message("Lấy danh sách lời mời đã gửi thành công")
                .result(friendshipService.getOutgoingRequests(currentUser))
                .build());
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<UserSuggestionDTO>>> getSuggestions(
            @AuthenticationPrincipal User currentUser
    ) {
        List<UserSuggestionDTO> result = friendshipService.getFriendSuggestions(currentUser);

        return ResponseEntity.ok(ApiResponse.<List<UserSuggestionDTO>>builder()
                .code(1000)
                .message("Lấy danh sách gợi ý thành công")
                .result(result)
                .build());
    }
}