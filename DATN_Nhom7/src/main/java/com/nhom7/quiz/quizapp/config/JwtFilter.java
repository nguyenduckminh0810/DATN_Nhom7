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

        // Danh sách endpoint public (không cần token)
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

        try {
            // Nếu có token thì parse và set Authentication (không phụ thuộc public/private)
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    if (username != null) {
                        User user = loginService.findByUsername(username);
                        String role = (user != null ? user.getRole() : "USER");
                        role = role == null ? "USER" : role.toUpperCase();

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

        // Cho request đi tiếp. Endpoint public sẽ pass nếu không có token;
        // endpoint bảo vệ sẽ dùng context đã set ở trên.
        filterChain.doFilter(request, response);
    }
}