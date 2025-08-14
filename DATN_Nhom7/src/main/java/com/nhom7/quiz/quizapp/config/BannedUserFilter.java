package com.nhom7.quiz.quizapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.UserRepo;

import java.io.IOException;
import java.util.Date;

@Component
public class BannedUserFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Chỉ kiểm tra khi đã đăng nhập (JWT hợp lệ)
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String username = auth.getName();
            User u = userRepo.findByUsername(username).orElse(null);

            boolean isBanned = u != null && (u.getRole() != null &&
                    "BANNED".equalsIgnoreCase(u.getRole()));

            if (isBanned) {
                // Xoá context và trả 403
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String json = """
                        {
                          "error":"ACCOUNT_BANNED",
                          "message":"Tài khoản của bạn đã bị ban. Vui lòng liên hệ quản trị viên.",
                          "username":"%s",
                          "endpoint":"%s",
                          "method":"%s",
                          "timestamp":"%s"
                        }
                        """.formatted(username, request.getRequestURI(), request.getMethod(), new Date());
                response.getWriter().write(json);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
