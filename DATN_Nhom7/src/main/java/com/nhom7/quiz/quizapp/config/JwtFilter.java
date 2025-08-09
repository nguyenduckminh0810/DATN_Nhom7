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

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        // 1) Bỏ qua OPTIONS
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

<<<<<<< HEAD
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
=======
        // 2) Danh sách endpoint cho phép truy cập không cần token
        boolean isPublicEndpoint = uri.startsWith("/api/login") ||
                uri.startsWith("/api/register") ||
                uri.equals("/api/admin/login") ||
                uri.startsWith("/api/image/quiz/") ||
                (uri.equals("/api/categories") && "GET".equalsIgnoreCase(method)) ||
                uri.startsWith("/api/user/avatars/") ||
                uri.startsWith("/api/upload/avatars/") ||
                uri.startsWith("/api/quiz/join/") ||
                uri.startsWith("/api/quiz/public/") ||
                uri.startsWith("/api/quiz/detail/") ||
                uri.startsWith("/api/quiz-attempts/public/") ||
                (uri.startsWith("/api/quizzes/") && "GET".equalsIgnoreCase(method)) ||
                uri.equals("/api/result/submit") ||
                uri.startsWith("/api/leaderboard/");
        // CHÚ Ý: ĐÃ GỠ /api/question/** ra khỏi list public

        try {
            // 3) Nếu có token thì luôn parse và set Authentication,
            // bất kể endpoint public hay không (không return sớm).
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
>>>>>>> 00b97f38 (làm chức năng quên MK,điều hướng result, fix userImportExcel)
                if (jwtUtil.validateToken(token)) {
String username = jwtUtil.extractUsername(token);
                    if (username != null) {
                        User user = loginService.findByUsername(username);

<<<<<<< HEAD
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
=======
                        String role = (user != null ? user.getRole() : "USER");
                        role = role == null ? "USER" : role.toUpperCase();
>>>>>>> 00b97f38 (làm chức năng quên MK,điều hướng result, fix userImportExcel)

                        // Prepend ROLE_
                        String authority = ("ADMIN".equals(role) || "ADMINISTRATOR".equals(role)
                                || "MODERATOR".equals(role))
                                        ? "ROLE_ADMIN"
                                        : "ROLE_USER";

                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, List.of(new SimpleGrantedAuthority(authority)));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            // log lỗi nhưng KHÔNG chặn request tại filter này
            e.printStackTrace();
        }

        // 4) Cho request đi tiếp. Endpoint public sẽ pass nếu không có token;
        // endpoint bảo vệ sẽ dùng context đã set ở trên.
        filterChain.doFilter(request, response);
    }
}