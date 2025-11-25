package com.hhy.apiserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // 1. Lấy key bí mật từ file application.yaml
    // Key này phải được mã hóa Base64
    @Value("${application.jwt.secretKey}")
    private String SECRET_KEY;

    // 2. Lấy thời gian hết hạn Access Token (mili-giây)
    @Value("${application.jwt.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;

    // 3. Lấy thời gian hết hạn Refresh Token (mili-giây)
    @Value("${application.jwt.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Trích xuất một claim (thông tin) cụ thể từ token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Tạo Access Token
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, ACCESS_TOKEN_EXPIRATION);
    }

    /**
     * Tạo Refresh Token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, REFRESH_TOKEN_EXPIRATION);
    }

    /**
     * Hàm private để tạo token (cả Access và Refresh)
     */
    private String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims) // Thêm các claim "phụ" (nếu có)
                .setSubject(userDetails.getUsername()) // Subject là username
                .setIssuedAt(new Date(System.currentTimeMillis())) // Ngày phát hành
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Ngày hết hạn
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Ký tên
                .compact(); // Build
    }

    /**
     * Kiểm tra xem token có hợp lệ không
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Kiểm tra xem token đã hết hạn chưa
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Lấy thời gian hết hạn từ token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Hàm private trích xuất TOÀN BỘ claims từ token
     * Hàm này sẽ xác thực chữ ký (nếu sai key nó sẽ văng Exception)
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Hàm private lấy Key dùng để ký (signing key)
     * Nó giải mã Base64 từ SECRET_KEY trong file .yaml
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
