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
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // ✅ THÊM ROLE VÀO TOKEN
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Phương thức này sẽ tạo token JWT cho người dùng (backward compatibility)
    public String generateToken(String username) {
        return generateToken(username, "USER"); // Default role
    }

    // Phương thức này sẽ trích xuất tên người dùng từ token
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }
    
    // ✅ THÊM METHOD ĐỂ EXTRACT ROLE TỪ TOKEN
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Phương thức này sẽ kiểm tra xem token có hợp lệ hay không
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            System.out.println("✅ Token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("❌ Token expired: " + e.getMessage());
            return false;
        } catch (JwtException e) {
            System.err.println("❌ Token invalid: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("❌ Token validation error: " + e.getMessage());
            return false;
        }
    }

    // Get claims method
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
