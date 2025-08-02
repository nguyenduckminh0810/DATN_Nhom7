package com.nhom7.quiz.quizapp.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    // Thời gian hết hạn của token (configurable)
    @Value("${jwt.expiration:86400000}") // Default 24 hours
    private long EXPIRATION_TIME;
    
    // Khóa bí mật từ environment hoặc application.properties
    @Value("${jwt.secret:my_super_secret_key_which_should_be_long_enough_for_production_use!}")
    private String jwtSecret;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Phương thức này sẽ tạo token JWT cho người dùng
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
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
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    // ✅ THÊM METHOD ĐỂ KIỂM TRA TOKEN HẾT HẠN
    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }
    
    // ✅ THÊM METHOD ĐỂ LẤY THỜI GIAN HẾT HẠN
    public Date getExpirationDateFromToken(String token) {
        return getClaims(token).getExpiration();
    }
}
