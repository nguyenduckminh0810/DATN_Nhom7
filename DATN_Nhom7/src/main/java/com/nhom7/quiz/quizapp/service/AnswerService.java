package com.nhom7.quiz.quizapp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.repository.AnswerRepo;

import jakarta.transaction.Transactional;

@Service
public class AnswerService {
    @Autowired
    private AnswerRepo answerRepo;

    public List<Answer> getAnswersByQuestionId(Long questionId) {
        return answerRepo.findByQuestionId(questionId);
    }

    public List<Answer> createMultipleAnswers(List<Answer> answers) {
        return answerRepo.saveAll(answers);
    }

    @Transactional
    public List<Answer> updateAnswers(List<Answer> updatedAnswers) {
        List<Answer> result = new ArrayList<>();

        for (Answer updated : updatedAnswers) {
            Optional<Answer> optional = answerRepo.findById(updated.getId());
            if (optional.isEmpty()) {
                throw new RuntimeException("Không tìm thấy câu trả lời với ID: " + updated.getId());
            }

            Answer existing = optional.get();
            existing.setContent(updated.getContent());
            existing.setCorrect(updated.isCorrect());

            result.add(answerRepo.save(existing));
        }

        return result;
    }

    public void deleteByQuestionId(Long questionId) {
        List<Answer> answers = answerRepo.findByQuestionId(questionId);
        if (answers != null && !answers.isEmpty()) {
            answerRepo.deleteAll(answers);
        }
    }
}
