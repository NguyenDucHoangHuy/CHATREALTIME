package com.hhy.apiserver.controller;

import com.hhy.apiserver.dto.request.auth.LoginRequestDTO;
import com.hhy.apiserver.dto.request.auth.LogoutRequestDTO;
import com.hhy.apiserver.dto.request.auth.RefreshTokenRequestDTO;
import com.hhy.apiserver.dto.request.auth.RegisterRequestDTO;
import com.hhy.apiserver.dto.response.ApiResponse;
import com.hhy.apiserver.dto.response.AuthResponseDTO;
import com.hhy.apiserver.dto.response.RefreshTokenResponseDTO;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.mapper.UserMapper;
import com.hhy.apiserver.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Tất cả API đều bắt đầu bằng /api/auth
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    /**
     * Endpoint Đăng Ký
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
            @RequestBody RegisterRequestDTO request
    ) {
        AuthResponseDTO result = authService.register(request);

        ApiResponse<AuthResponseDTO> response = ApiResponse.<AuthResponseDTO>builder()
                .code(1000)
                .message("Đăng ký thành công")
                .result(result)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint Đăng Nhập
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @RequestBody LoginRequestDTO request
    ) {
        AuthResponseDTO result = authService.login(request);

        ApiResponse<AuthResponseDTO> response = ApiResponse.<AuthResponseDTO>builder()
                .code(1000)
                .message("Đăng nhập thành công")
                .result(result)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint Làm Mới Token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshToken(
            @RequestBody RefreshTokenRequestDTO request
    ) {
        RefreshTokenResponseDTO result = authService.refreshToken(request);

        ApiResponse<RefreshTokenResponseDTO> response = ApiResponse.<RefreshTokenResponseDTO>builder()
                .code(1000)
                .message("Làm mới token thành công")
                .result(result)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint Đăng Xuất
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestBody LogoutRequestDTO request
    ) {
        authService.logout(request);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(1000)
                .message("Đăng xuất thành công")
                .result(null) // Logout không cần trả về dữ liệu
                .build();

        return ResponseEntity.ok(response);
    }


    /**
     * Endpoint Lấy thông tin Profile
     * GET /api/auth/me
     * (API này sẽ được SecurityConfig bảo vệ)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getMyProfile(
            // 1. Dùng @AuthenticationPrincipal để Spring
            // tự động tiêm object User (đã được Filter xác thực) vào đây
            @AuthenticationPrincipal User currentUser
    ) {
        // 2. Dùng mapper để chuyển đổi an toàn sang DTO
        // (currentUser là User Entity đầy đủ)
        UserDTO userDTO = userMapper.toUserDTO(currentUser);

        // 3. Xây dựng và trả về ApiResponse
        ApiResponse<UserDTO> response = ApiResponse.<UserDTO>builder()
                .code(1000)
                .message("Lấy thông tin profile thành công")
                .result(userDTO)
                .build();

        return ResponseEntity.ok(response);
    }
}
