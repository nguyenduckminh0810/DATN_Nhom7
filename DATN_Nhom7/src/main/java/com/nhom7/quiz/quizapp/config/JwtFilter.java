package com.nhom7.quiz.quizapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.model.User;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private LoginService loginService;

    // ✅ REMOVED unused PUBLIC_ENDPOINT_PREFIXES - logic moved inline for better maintainability

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        System.out.println("🔍 JWT Filter - URI: " + requestURI + " | Method: " + method);

        // ✅ BỎ QUA OPTIONS REQUEST
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("✅ Skipping OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ DANH SÁCH PUBLIC ENDPOINTS - HOÀN TOÀN THỐNG NHẤT VỚI SECURITY CONFIG
        boolean isPublicEndpoint = requestURI.startsWith("/api/login") ||
                requestURI.startsWith("/api/register") ||
                requestURI.equals("/api/admin/login") ||  // ✅ Admin login public
                requestURI.startsWith("/api/image/quiz/") ||
                (requestURI.equals("/api/categories") && "GET".equalsIgnoreCase(method)) ||
                requestURI.startsWith("/api/user/avatars/") ||
                requestURI.startsWith("/api/upload/avatars/") ||
                requestURI.startsWith("/api/quiz/join/") ||
                requestURI.startsWith("/api/quiz/public/") ||  // ✅ Thêm "/" cuối
                requestURI.startsWith("/api/quiz/detail/") || // ✅ Thêm "/" cuối  
                requestURI.startsWith("/api/question/") ||

                requestURI.startsWith("/api/quiz-attempts/public/") ||
                (requestURI.startsWith("/api/quizzes/") && "GET".equalsIgnoreCase(method)) || // ✅ Only GET quizzes public
                requestURI.equals("/api/result/submit");

                requestURI.startsWith("/api/quiz/public") ||
                requestURI.startsWith("/api/leaderboard/")) {


        if (isPublicEndpoint) {
            System.out.println("✅ JWT Filter - Skipping public endpoint: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ XỬ LÝ JWT TOKEN
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("🔍 Processing JWT token...");
            
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    
                    if (username != null) {
                        User user = loginService.findByUsername(username);
                        List<SimpleGrantedAuthority> authorities;

                        if (user != null) {
                            String role = user.getRole().toUpperCase(); // ✅ ENSURE UPPERCASE
                            String authority;
                            
                            // ✅ IMPROVED ROLE MAPPING - Support multiple admin roles
                            if ("ADMIN".equals(role) || "ADMINISTRATOR".equals(role) || "MODERATOR".equals(role)) {
                                authority = "ROLE_ADMIN";
                            } else {
                                authority = "ROLE_USER";
                            }
                            
                            authorities = List.of(new SimpleGrantedAuthority(authority));
                            System.out.println(
                                    "🔐 User: " + username + " | Role: " + role + " | Authority: " + authority);
                        } else {
                            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                            System.out.println("⚠️ User not found: " + username + " | Default Authority: ROLE_USER");
                        }

                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        System.out.println("✅ Authentication successful: " + username);
                    }
                } else {
                    System.out.println("❌ Invalid or expired token");
                }
            } catch (Exception e) {
                System.err.println("❌ JWT processing error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("❌ No Bearer token found for protected endpoint: " + requestURI);
        }

        filterChain.doFilter(request, response);
    }

    // ✅ REMOVED unused isPublicEndpoint method - logic moved inline
}
