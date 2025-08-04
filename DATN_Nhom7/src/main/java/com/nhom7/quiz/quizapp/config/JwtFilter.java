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
import java.util.Collections;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

// ✅ THÊM IMPORT ĐỂ ĐỌC USER TỪ DATABASE
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.model.User;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // ✅ THÊM LOGINSERVICE ĐỂ ĐỌC USER TỪ DATABASE
    @Autowired
    private LoginService loginService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        String method = request.getMethod();

        System.out.println("🔍 JWT Filter - Request URI: " + requestURI);
        System.out.println("🔍 JWT Filter - Method: " + method);
        System.out.println("🔍 JWT Filter - Auth Header: " + (authHeader != null ? "Present" : "Not present"));

        // ✅ BỎ QUA OPTIONS REQUESTS (CORS PREFLIGHT)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("✅ JWT Filter - Skipping OPTIONS request");
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ BỎ QUA PUBLIC ENDPOINTS
        if (requestURI.startsWith("/api/login") ||
                requestURI.startsWith("/api/register") ||
                requestURI.startsWith("/api/image/quiz/") ||
                requestURI.startsWith("/api/categories") ||
                requestURI.startsWith("/api/user/avatars/") ||
                requestURI.startsWith("/api/upload/avatars/") ||
                requestURI.startsWith("/api/quiz-attempts/public/") ||
                requestURI.startsWith("/api/public/") ||
                requestURI.startsWith("/api/quiz/") && requestURI.contains("/detail") ||
                requestURI.startsWith("/api/question/") ||
                requestURI.startsWith("/api/quiz/public") ||
                requestURI.startsWith("/api/quizzes/")) {

            System.out.println("✅ JWT Filter - Skipping public endpoint: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ CHỈ XỬ LÝ JWT NẾU CÓ AUTH HEADER
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // ✅ KIỂM TRA TOKEN HỢP LỆ VÀ CHƯA HẾT HẠN
            if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                String username = jwtUtil.extractUsername(token);
                if (username != null) {
                    try {
                        // ✅ ĐỌC USER TỪ DATABASE ĐỂ LẤY ROLE CHÍNH XÁC
                        User user = loginService.findByUsername(username);
                        List<SimpleGrantedAuthority> authorities;

                        if (user != null) {
                            // ✅ SỬ DỤNG ROLE TỪ DATABASE
                            String role = user.getRole();
                            if ("ADMIN".equalsIgnoreCase(role)) {
                                authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                            } else {
                                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                            }
                        } else {
                            // Fallback nếu không tìm thấy user
                            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                        }

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("✅ JWT Filter - Authentication set for user: " + username);
                    } catch (Exception e) {
                        // ✅ LOG ERROR VÀ TIẾP TỤC REQUEST
                        System.err.println("❌ Error in JWT filter: " + e.getMessage());
                        // Không set authentication, để request tiếp tục
                    }
                }
            } else {
                // ✅ LOG INVALID TOKEN
                System.out.println("⚠️ Invalid or expired token");
            }
        } else {
            System.out.println("ℹ️ No JWT token found, continuing with request");
        }

        // ✅ LUÔN TIẾP TỤC FILTER CHAIN
        filterChain.doFilter(request, response);
    }
}
