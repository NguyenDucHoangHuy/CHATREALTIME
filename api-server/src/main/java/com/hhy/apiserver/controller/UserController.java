package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.request.user.UpdateProfileRequestDTO;
import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // Các API bắt đầu bằng /api/users
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Endpoint Cập nhật thông tin cá nhân
     * PUT /api/users/me
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateMyProfile(
            @AuthenticationPrincipal User currentUser, // Lấy user từ token
            @RequestBody UpdateProfileRequestDTO request
    ) {
        UserDTO updatedProfile = userService.updateProfile(currentUser, request);

        ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
                .code(1000)
                .message("Cập nhật hồ sơ thành công")
                .result(updatedProfile)
                .build();

        return ResponseEntity.ok(response);
    }
}