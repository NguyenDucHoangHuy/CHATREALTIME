package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.request.message.MarkReadRequestDTO;
import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.dto.response.MessageDTO;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 1. Tải lịch sử tin nhắn
    // GET /api/messages/history?conversationId=10&page=0&size=20
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Sắp xếp giảm dần theo thời gian (tin mới nhất nằm đầu danh sách trả về)
        // Frontend khi nhận về sẽ đảo ngược lại hoặc prepend vào list
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(ApiResponse.<Page<MessageDTO>>builder()
                .code(1000).message("Tải lịch sử thành công")
                .result(messageService.getMessages(currentUser, conversationId, pageable))
                .build());
    }

    // 2. Xóa tin nhắn
    // DELETE /api/messages/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        messageService.deleteMessage(currentUser, id);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã xóa tin nhắn")
                .build());
    }

    // 3. Đánh dấu đã đọc
    // POST /api/messages/read
    @PostMapping("/read")
    public ResponseEntity<ApiResponse<String>> markAsRead(
            @AuthenticationPrincipal User currentUser,
            @RequestBody MarkReadRequestDTO request) {

        messageService.markAsRead(currentUser, request);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000).message("Đã đánh dấu đã đọc")
                .build());
    }
}