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

    private static final List<String> PUBLIC_ENDPOINT_PREFIXES = List.of(
            "/api/login",
            "/api/register",
            "/api/image/quiz/",
            "/api/categories",
            "/api/user/avatars/",
            "/api/upload/avatars/",
            "/api/quiz-attempts/public/",
            "/api/public/",
            "/api/question/play/",
            "/api/quiz/public",
            "/api/quiz/detail",
            "/api/result/submit");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        System.out.println("🔍 JWT Filter - URI: " + requestURI);
        System.out.println("🔍 JWT Filter - Method: " + method);
        System.out.println("🔍 JWT Filter - Auth Header: " + (authHeader != null ? "✅ Present" : "❌ Not Present"));

        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("✅ Skipping OPTIONS request");
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

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                
                if (username != null) {
                    try {
                        User user = loginService.findByUsername(username);
                        List<SimpleGrantedAuthority> authorities;

                        if (user != null) {
                            String role = user.getRole();
                            authorities = List.of(new SimpleGrantedAuthority(
                                    "ADMIN".equalsIgnoreCase(role) ? "ROLE_ADMIN" : "ROLE_USER"));
                        } else {
                            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                        }
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, authorities);

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        System.out.println("✅ Authenticated user: " + username);
                    } catch (Exception e) {
                        System.err.println("❌ JWT filter error: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("⚠️ Invalid or expired token");
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String uri) {
        return PUBLIC_ENDPOINT_PREFIXES.stream().anyMatch(uri::startsWith);
    }
}
