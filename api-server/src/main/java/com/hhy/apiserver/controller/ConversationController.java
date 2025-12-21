package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.request.conversation.CreateGroupRequestDTO;
import com.hhy.apiserver.dto.request.conversation.OpenConversationRequestDTO;
import com.hhy.apiserver.dto.request.conversation.UpdateGroupInfoRequestDTO;
import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.dto.response.ConversationDTO;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // 1. Mở chat 1-1
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<ConversationDTO>> openOneToOne(
            @AuthenticationPrincipal User currentUser,
            @RequestBody OpenConversationRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.<ConversationDTO>builder()
                .code(1000).message("Thành công")
                .result(conversationService.openOneToOneConversation(currentUser, request))
                .build());
    }

    // 2. Tạo nhóm
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<ConversationDTO>> createGroup(
            @AuthenticationPrincipal User currentUser,
            @RequestBody CreateGroupRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.<ConversationDTO>builder()
                .code(1000).message("Tạo nhóm thành công")
                .result(conversationService.createGroup(currentUser, request))
                .build());
    }

    // 3. Lấy danh sách
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getMyConversations(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(ApiResponse.<List<ConversationDTO>>builder()
                .code(1000).message("Lấy danh sách thành công")
                .result(conversationService.getMyConversations(currentUser))
                .build());
    }

    // 5. Xóa (Ẩn) hội thoại
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<String>> deleteConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {

        conversationService.deleteConversation(currentUser, conversationId);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã xóa cuộc trò chuyện")
                .build());
    }

    // 6. Lấy danh sách tin nhắn đang chờ
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getPendingConversations(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(ApiResponse.<List<ConversationDTO>>builder()
                .code(1000).message("Lấy danh sách tin nhắn chờ thành công")
                .result(conversationService.getPendingConversations(currentUser))
                .build());
    }

    // 7. Chấp nhận tin nhắn đang chờ
    @PutMapping("/{conversationId}/accept")
    public ResponseEntity<ApiResponse<String>> acceptConversation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {

        conversationService.acceptPendingConversation(currentUser, conversationId);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã chấp nhận tin nhắn")
                .build());
    }

    // 8. Lấy chi tiết cuộc trò chuyện (Info)
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationDTO>> getDetails(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long conversationId) {

        return ResponseEntity.ok(ApiResponse.<ConversationDTO>builder()
                .code(1000)
                .message("Lấy thông tin thành công")
                .result(conversationService.getConversationDetails(currentUser, conversationId))
                .build());
    }

    // 9. Cập nhật thông tin nhóm (Tên, Ảnh) - Thay thế cho renameGroup cũ
    @PutMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationDTO>> updateGroupInfo(
            @PathVariable Long conversationId,
            @RequestBody UpdateGroupInfoRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.<ConversationDTO>builder()
                .code(1000)
                .message("Cập nhật nhóm thành công")
                .result(conversationService.updateGroupInfo(conversationId, request))
                .build());
    }
}