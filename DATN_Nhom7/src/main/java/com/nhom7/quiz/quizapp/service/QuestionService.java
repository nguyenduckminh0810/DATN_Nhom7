package com.nhom7.quiz.quizapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Question;
import com.nhom7.quiz.quizapp.repository.QuestionRepo;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepo questionRepo;

    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepo.findByQuizId(quizId);
    }

    public Question updateQuestion(Long id, Question updatedQuestion) {
        Optional<Question> optional = questionRepo.findById(id);
        if (optional.isEmpty()) {
            return null;
        }

        Question existing = optional.get();
        existing.setContent(updatedQuestion.getContent());
        existing.setPoint(updatedQuestion.getPoint());

        return questionRepo.save(existing);
    }

    public Question createQuestion(Question question) {
        return questionRepo.save(question);
    }

    public boolean deleteQuestion(Long questionId) {
        Optional<Question> optional = questionRepo.findById(questionId);
        if (optional.isEmpty()) {
            return false;
        }
        questionRepo.delete(optional.get());
        return true;
    }
}
