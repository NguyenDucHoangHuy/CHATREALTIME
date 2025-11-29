package com.hhy.apiserver.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration // Đánh dấu đây là file Cấu hình
@EnableWebSecurity // Kích hoạt Spring Security
@EnableMethodSecurity // (Tùy chọn) Bật bảo mật ở cấp độ HÀM (nếu sau này bạn cần)
@RequiredArgsConstructor
public class SecurityConfig {

    // 1. Tiêm (Inject) Filter và Provider mà chúng ta đã tạo
    private final JwtAuthenticationFilter jwtAuthFilter; // (Từ Bước 4)
    private final AuthenticationProvider authenticationProvider; // (Từ Bước 3.2 - ApplicationConfig)

    // 2. Định nghĩa "Chuỗi lọc bảo mật" (SecurityFilterChain)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 3. Vô hiệu hóa CSRF (Cross-Site Request Forgery)
        // Chúng ta dùng JWT (stateless), nên không cần CSRF.
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // 4. CẤU HÌNH CÁC ĐƯỜNG DẪN (Authorization Rules)
        http.authorizeHttpRequests(auth -> auth
                // 4a. Cho phép (permitAll) tất cả các API trong /api/auth/**
                // Bao gồm: /api/auth/register, /api/auth/login, /api/auth/refresh
                .requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/logout"
                )
                .permitAll()

                // 4b. Mọi request khác (anyRequest)
                // Đều BẮT BUỘC phải được xác thực (authenticated)
                .anyRequest()
                .authenticated()
        );

        // 5. Cấu hình quản lý phiên (Session Management)
        // Yêu cầu Spring Security KHÔNG TẠO MỚI HOẶC SỬ DỤNG Session (STATELESS)
        // Vì chúng ta dùng JWT, mọi request đều độc lập.
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // 6. Gắn "Bộ não" xác thực (AuthenticationProvider)
        // (Đã được định nghĩa ở ApplicationConfig,
        // nó biết cách dùng UserDetailsService và PasswordEncoder)
        http.authenticationProvider(authenticationProvider);

        // 7. GẮN FILTER CỦA CHÚNG TA (JwtAuthenticationFilter) VÀO CHUỖI
        // Yêu cầu: Chạy filter 'jwtAuthFilter' (của Bước 4)
        // TRƯỚC khi chạy filter 'UsernamePasswordAuthenticationFilter' (của Spring)
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Dùng Pattern "*" thay vì Origin "*" để linh hoạt hơn
        // Nó cho phép MỌI IP, MỌI Port gọi vào
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Cho phép các method
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cho phép các header (đặc biệt là Authorization)
        configuration.setAllowedHeaders(List.of("*"));

        // Đăng ký cấu hình này cho mọi endpoint
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}