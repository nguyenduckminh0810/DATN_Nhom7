package com.nhom7.quiz.quizapp.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    // Thời gian hết hạn của token (1 ngày)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 1 ngày
    // Khóa bí mật để ký token, cần đủ dài (ít nhất 256 bit cho HS256)
    private static final SecretKey SECRET_KEY = Keys
            .hmacShaKeyFor("my_super_secret_key_which_should_be_long_enough!".getBytes());

    // Phương thức này sẽ tạo token JWT cho người dùng
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Phương thức này sẽ trích xuất tên người dùng từ token
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Phương thức này sẽ kiểm tra xem token có hợp lệ hay không
    public boolean validateToken(String token) {
        try {
            getClaims(token); // Nếu token không hợp lệ, sẽ ném lỗi
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("JWT Validation Error: " + e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
