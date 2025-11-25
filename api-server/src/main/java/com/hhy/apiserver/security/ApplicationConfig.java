package com.hhy.apiserver.security;

import com.hhy.apiserver.exception.AppException;
import com.hhy.apiserver.exception.ErrorCode;
import com.hhy.apiserver.respository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration //app start scan qua thì cái class config này sẽ chạy đầu sẽ tạo instance (bean) của ApplicationConfig.
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserRepository userRepository;

    /**
     * Bean này "dạy" Spring cách tìm user
     * Nó sẽ được JwtAuthenticationFilter và AuthService sử dụng
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return usernameOrEmailOrPhone -> {
            // Nâng cấp logic:
            // Thử tìm bằng Username trước
            return userRepository.findByUsername(usernameOrEmailOrPhone)
                    // Nếu không thấy, thử tìm bằng Email
                    .or(() -> userRepository.findByEmail(usernameOrEmailOrPhone))
                    // Nếu vẫn không thấy, thử tìm bằng SĐT
                    .or(() -> userRepository.findByPhoneNumber(usernameOrEmailOrPhone))
                    // Nếu không tìm thấy bằng cả 3, ném lỗi
                    .orElseThrow(() ->
                            new AppException(ErrorCode.LOGIN_FAILED)
                    );
        };
    }

    /**
     * Bean này định nghĩa thuật toán hash mật khẩu
     * Nó sẽ được AuthService (lúc đăng ký)
     * và AuthenticationProvider (lúc đăng nhập) sử dụng
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean này là "bộ não" xử lý xác thực (username, password)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService()); // Dùng bean ở trên
        authProvider.setPasswordEncoder(passwordEncoder()); // Dùng bean ở trên
        return authProvider;
    }

    /**
     * Quản lý các AuthenticationProvider
     * Cần cho việc xác thực lúc đăng nhập
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}