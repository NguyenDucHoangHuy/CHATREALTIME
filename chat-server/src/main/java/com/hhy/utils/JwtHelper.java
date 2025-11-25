package com.hhy.utils;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;

public class JwtHelper {


    public static final String SECRET_KEY = System.getenv("SECRET_KEY_CHATREALTIME");

    // Hàm trích xuất username từ token
    public static String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            // Token hết hạn hoặc không hợp lệ
            return null;
        }
    }

    // Hàm giải mã Key
    private static Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}