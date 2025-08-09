package com.nhom7.quiz.quizapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nhom7.quiz.quizapp.service.auth.PasswordResetOtpService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final PasswordResetOtpService otpService;

    public AuthController(PasswordResetOtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/forgot-password-code")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> body) {
        String emailOrUsername = body.getOrDefault("emailOrUsername", "").trim();
        otpService.requestCode(emailOrUsername);
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
        String emailOrUsername = body.getOrDefault("emailOrUsername", "").trim();
        String code = body.getOrDefault("code", "").trim();
        String newPassword = body.getOrDefault("newPassword", "");
        boolean ok = otpService.verifyAndReset(emailOrUsername, code, newPassword);
        if (!ok) return ResponseEntity.badRequest().body(Map.of("status", "INVALID_OR_EXPIRED"));
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }
}


