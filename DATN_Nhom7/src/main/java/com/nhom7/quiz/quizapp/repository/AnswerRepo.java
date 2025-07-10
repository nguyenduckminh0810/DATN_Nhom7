package com.nhom7.quiz.quizapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nhom7.quiz.quizapp.model.Answer;

@Repository
public interface AnswerRepo extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestionId(Long questionId);

    Optional<Answer> findByQuestion_IdAndIsCorrectTrue(Long questionId);

}
