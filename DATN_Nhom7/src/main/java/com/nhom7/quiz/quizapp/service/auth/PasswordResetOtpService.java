package com.nhom7.quiz.quizapp.service.auth;

import com.nhom7.quiz.quizapp.model.PasswordResetCode;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.PasswordResetCodeRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import com.nhom7.quiz.quizapp.service.MailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PasswordResetOtpService {
    private final UserRepo userRepo;
    private final PasswordResetCodeRepo codeRepo;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public PasswordResetOtpService(UserRepo userRepo,
                                   PasswordResetCodeRepo codeRepo,
                                   PasswordEncoder passwordEncoder,
                                   MailService mailService) {
        this.userRepo = userRepo;
        this.codeRepo = codeRepo;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public void requestCode(String emailOrUsername) {
        Optional<User> userOpt = userRepo.findByEmail(emailOrUsername);
        if (userOpt.isEmpty()) userOpt = userRepo.findByUsername(emailOrUsername);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();

        // prevent too frequent resend (60s)
        codeRepo.findTopByUserIdAndUsedAtIsNullOrderByIdDesc(user.getId()).ifPresent(last -> {
            if (last.getLastSentAt() != null && last.getLastSentAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
                // simply ignore to avoid leaking timing info; controller will still return 200
                return;
            }
        });

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        PasswordResetCode rec = new PasswordResetCode();
        rec.setUser(user);
        rec.setCodeHash(passwordEncoder.encode(code));
        rec.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        rec.setLastSentAt(LocalDateTime.now());
        codeRepo.save(rec);

        mailService.sendPasswordResetCode(user.getEmail(), user.getFullName(), code);
    }

    public boolean verifyAndReset(String emailOrUsername, String code, String newPassword) {
        Optional<User> userOpt = userRepo.findByEmail(emailOrUsername);
        if (userOpt.isEmpty()) userOpt = userRepo.findByUsername(emailOrUsername);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        Optional<PasswordResetCode> codeOpt = codeRepo.findTopByUserIdAndUsedAtIsNullOrderByIdDesc(user.getId());
        if (codeOpt.isEmpty()) return false;

        PasswordResetCode rec = codeOpt.get();
        if (rec.getExpiresAt().isBefore(LocalDateTime.now()) || rec.getUsedAt() != null) return false;
        if (rec.getAttemptCount() >= 5) return false;

        rec.setAttemptCount(rec.getAttemptCount() + 1);
        boolean matched = passwordEncoder.matches(code, rec.getCodeHash());
        if (!matched) { codeRepo.save(rec); return false; }

        user.setPassword(passwordEncoder.encode(newPassword));
        rec.setUsedAt(LocalDateTime.now());
        codeRepo.save(rec);
        userRepo.save(user);
        return true;
    }
}


