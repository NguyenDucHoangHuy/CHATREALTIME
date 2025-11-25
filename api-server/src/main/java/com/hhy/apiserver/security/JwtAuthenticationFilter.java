package com.hhy.apiserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Đánh dấu đây là một Bean, Spring sẽ tự động nhận diện
@RequiredArgsConstructor // Tự động tạo constructor cho các trường 'final'
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // (Từ Bước 2)
    private final UserDetailsService userDetailsService; // (Từ Bước 3)

    // Đây là hàm cốt lõi của Filter
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy header "Authorization" từ request
        final String authHeader = request.getHeader("Authorization");

        // 2. Kiểm tra xem header có tồn tại và có bắt đầu bằng "Bearer " không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Nếu không có token, cho request đi tiếp
            // (Nó sẽ bị chặn ở SecurityConfig nếu API đó yêu cầu xác thực)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Nếu có, tách lấy phần token (bỏ "Bearer")
        final String jwt = authHeader.substring(7); // "Bearer ".length() == 7

        // 4. Trích xuất username (subject) từ token
        final String username = jwtService.extractUsername(jwt);

        // 5. Kiểm tra: nếu có username VÀ user chưa được xác thực
        // (SecurityContextHolder.getContext().getAuthentication() == null)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Tải thông tin UserDetails (chính là object User) từ CSDL
            // Bean 'userDetailsService' này đã được định nghĩa ở Bước 3
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 7. Kiểm tra xem token có hợp lệ không (cả chữ ký và thời gian)
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 8. Nếu token hợp lệ, tạo một "vé" (Token) xác thực
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // Chủ thể (principal)
                        null,        // Mật khẩu (không cần)
                        userDetails.getAuthorities() // Quyền hạn (roles)
                );

                // 9. Ghi thêm chi tiết (ví dụ: IP, session) vào "vé"
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 10. ĐẶT "VÉ" VÀO SECURITY CONTEXT
                // Đây là bước quan trọng nhất: báo cho Spring Security "User này đã được xác thực!"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. Cho request đi tiếp đến Filter tiếp theo (hoặc Controller)
        filterChain.doFilter(request, response);
    }
}








//Đây là "người gác cổng" (gatekeeper) cho hệ thống của bạn. Nó sẽ chạy trước tất cả các API, chặn mọi request,
// kiểm tra Access Token, và quyết định xem có "cho" request đó đi tiếp hay không.

//Không có "Bearer" Token: Cho đi qua (sẽ bị SecurityConfig chặn sau).

//Có "Bearer" Token: Giải mã, tìm user trong CSDL, xác thực. Nếu OK, "đăng nhập" user đó vào SecurityContext và cho đi qua.