package com.nhom7.quiz.quizapp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.nhom7.quiz.quizapp.model.PasswordResetCode;

public interface PasswordResetCodeRepo extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findTopByUserIdAndUsedAtIsNullOrderByIdDesc(Long userId);
}


