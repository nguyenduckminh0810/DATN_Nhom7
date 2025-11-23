package com.nhom7.quiz.quizapp.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException e,
            HttpServletRequest request) {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        String userRole = auth != null && !auth.getAuthorities().isEmpty()
                ? auth.getAuthorities().iterator().next().getAuthority()
                : "NO_ROLE";

        // LOGGING CHI TIẾT
        System.err.println("ACCESS DENIED - " + new Date());
        System.err.println("User: " + username);
        System.err.println("Role: " + userRole);
        System.err.println("Endpoint: " + method + " " + uri);
        System.err.println("Reason: " + e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "ACCESS_DENIED",
                "message", "Bạn không có quyền truy cập endpoint này",
                "endpoint", uri,
                "method", method,
                "userRole", userRole,
                "timestamp", new Date(),
                "status", 403));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e, HttpServletRequest request) {
        System.err.println("GENERAL ERROR: " + e.getMessage());
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Đã xảy ra lỗi hệ thống",
                "endpoint", request.getRequestURI(),
                "timestamp", new Date(),
                "status", 500));
    }
}