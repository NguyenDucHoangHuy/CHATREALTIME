package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    /**
     * 1. Tìm kiếm TẤT CẢ user trong hệ thống
     * GET /api/search/user?q=hoang
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(
            @RequestParam("q") String query,
            @AuthenticationPrincipal User currentUser
    ) {
        // Gọi service để tìm
        List<UserDTO> results = searchService.searchUsers(query, currentUser.getUserId());

        ApiResponse<List<UserDTO>> response = ApiResponse.<List<UserDTO>>builder()
                .code(1000)
                .message("Tìm kiếm người dùng thành công")
                .result(results)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 2. Tìm kiếm TRONG DANH SÁCH BẠN BÈ
     * GET /api/search/friends?q=huy
     */
    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchFriends(
            @RequestParam("q") String query,
            @AuthenticationPrincipal User currentUser
    ) {
        // Gọi service, truyền cả 'currentUser' và 'query'
        List<UserDTO> results = searchService.searchFriends(currentUser, query);

        ApiResponse<List<UserDTO>> response = ApiResponse.<List<UserDTO>>builder()
                .code(1000)
                .message("Tìm kiếm bạn bè thành công")
                .result(results)
                .build();
        return ResponseEntity.ok(response);
    }
}