package com.hhy.apiserver.service;

import com.hhy.apiserver.dto.request.auth.LoginRequestDTO;
import com.hhy.apiserver.dto.request.auth.LogoutRequestDTO;
import com.hhy.apiserver.dto.request.auth.RefreshTokenRequestDTO;
import com.hhy.apiserver.dto.request.auth.RegisterRequestDTO;
import com.hhy.apiserver.dto.response.AuthResponseDTO;
import com.hhy.apiserver.dto.response.RefreshTokenResponseDTO;
import com.hhy.apiserver.dto.response.UserDTO;
import com.hhy.apiserver.entity.RefreshToken;
import com.hhy.apiserver.entity.User;
import com.hhy.apiserver.exception.AppException;
import com.hhy.apiserver.exception.ErrorCode;
import com.hhy.apiserver.mapper.UserMapper;
import com.hhy.apiserver.respository.RefreshTokenRepository;
import com.hhy.apiserver.respository.UserRepository;
import com.hhy.apiserver.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    // Tiêm (Inject) tất cả các "công cụ" chúng ta đã tạo
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    /**
     * API Đăng Ký
     */
    @Transactional // Đảm bảo tất cả cùng thành công hoặc cùng thất bại
    public AuthResponseDTO register(RegisterRequestDTO requestDTO) {

        // 1. Kiểm tra username/email/phone đã tồn tại chưa
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        if (userRepository.existsByPhoneNumber(requestDTO.getPhoneNumber())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // 2. Dùng MapStruct để chuyển DTO -> Entity
        User user = userMapper.toUser(requestDTO);

        // 3. Hash mật khẩu (rất quan trọng)
        user.setPasswordHash(passwordEncoder.encode(requestDTO.getPassword()));

        // Vì đăng ký xong là vào app luôn, nên set luôn là ONLINE
        user.setOnlineStatus(User.OnlineStatus.online);

        // 4. Lưu User mới vào CSDL
        User savedUser = userRepository.save(user);

        // 5. Tạo Access Token và Refresh Token
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        // 6. Lưu Refresh Token vào CSDL
        saveRefreshToken(savedUser, refreshToken);

        // 7. Chuyển Entity -> DTO để trả về
        UserDTO userDTO = userMapper.toUserDTO(savedUser);

        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    /**
     * API Đăng Nhập
     */
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        // 1. Xác thực
        // Biến 'authentication' sẽ chứa thông tin user nếu thành công
        Authentication authentication;
        try {
            // 1. "Dạy" UserDetailsService tìm user bằng cả 3 (xem Ghi chú bên dưới)
            // Tạm thời: Logic tìm user sẽ nằm trong ApplicationConfig
            // Spring Security (AuthenticationManager) sẽ tự động
            // gọi UserDetailsService và PasswordEncoder để kiểm tra
            //authenticationManager sẽ dùng DaoAuthenticationProvider và gọi userDetailsService()
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDTO.getLoginIdentifier(),
                            requestDTO.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            // Nếu sai pass hoặc không tìm thấy user
            throw new AppException(ErrorCode.LOGIN_FAILED);
        }

        // 2. Nếu thành công, lấy User object từ 'authentication'
        // Không cần gọi .findByEmail(...) nữa!
        User user = (User) authentication.getPrincipal();

        // 3. Tạo token mới
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. Lưu (hoặc cập nhật) Refresh Token
        saveRefreshToken(user, refreshToken);

        // 5. Trả về
        UserDTO userDTO = userMapper.toUserDTO(user);
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    /**
     * API Làm Mới Token
     */
    @Transactional
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO requestDTO) {
        // 1. Tìm Refresh Token trong CSDL
        RefreshToken rt = refreshTokenRepository.findByToken(requestDTO.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED)); // Token không tồn tại

        // 2. Kiểm tra xem token đã hết hạn chưa
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt); // Xóa token hết hạn
            throw new AppException(ErrorCode.UNAUTHENTICATED); // Bắt đăng nhập lại
        }

        // 3. Lấy user từ token
        User user = rt.getUser();

        // 4. Tạo Access Token MỚI
        String newAccessToken = jwtService.generateAccessToken(user);
        log.info("Đã refresh tạo accessToken mới rồi");

        return new RefreshTokenResponseDTO(newAccessToken);
    }

    /**
     * Hàm private để lưu/cập nhật Refresh Token
     */
    private void saveRefreshToken(User user, String token) {
        // Có thể user đã đăng nhập ở máy khác, ta tìm xem có token cũ không
        refreshTokenRepository.findByUserUserId(user.getUserId()).ifPresent(refreshTokenRepository::delete);

        // Tạo token mới
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setToken(token);
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION));

        refreshTokenRepository.save(newRefreshToken);
    }

    /**
     * API Đăng Xuất
     * Xóa Refresh Token khỏi CSDL
     */
    @Transactional
    public void logout(LogoutRequestDTO requestDTO) {
        // 1. Tìm Refresh Token trong CSDL
        RefreshToken rt = refreshTokenRepository.findByToken(requestDTO.getRefreshToken())
                .orElse(null); // Tìm, nếu không thấy thì thôi (đã logout)

        // 2. Nếu tìm thấy, xóa nó đi
        if (rt != null) {
            refreshTokenRepository.delete(rt);
        }

        // Bất kể tìm thấy hay không, client cũng đã yêu cầu logout,
        // và phía client họ cũng đã tự xóa token.
        // Ta không cần ném lỗi nếu không tìm thấy.
    }
}