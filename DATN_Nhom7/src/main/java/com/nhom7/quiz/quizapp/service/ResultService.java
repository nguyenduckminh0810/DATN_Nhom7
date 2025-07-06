package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.QuizSubmissionDTO;
import com.nhom7.quiz.quizapp.repository.AnswerRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.repository.ResultRepo;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class ResultService {

    @Autowired
    private AnswerRepo answerRepo;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private QuizRepo quizRepo;

    public int evaluateAndSave(QuizSubmissionDTO submission) {
        int correctCount = 0;

        for (QuizSubmissionDTO.AnswerSubmission ans : submission.getAnswers()) {
            Optional<Answer> answerOpt = answerRepo.findById(ans.getAnswerId());
            if (answerOpt.isPresent() && answerOpt.get().isCorrect()) {
                correctCount++;
            }
        }

        int total = submission.getAnswers().size();
        int score = (int) ((correctCount / (double) total) * 100);

        // Lấy User và Quiz từ ID
        Optional<User> userOpt = userRepo.findById(submission.getUserId());
        Optional<Quiz> quizOpt = quizRepo.findById(submission.getQuizId());

        if (userOpt.isEmpty() || quizOpt.isEmpty()) {
            throw new IllegalArgumentException("User hoặc Quiz không tồn tại.");
        }

        Result result = new Result();
        result.setUser(userOpt.get());
        result.setQuiz(quizOpt.get());
        result.setScore(score);
        result.setCompletedAt(LocalDateTime.now());

        resultRepo.save(result);

        return score;
    }

    public List<Result> getResultsByUserId(Long userId) {
        return resultRepo.findAllByUser_Id(userId); // hoặc tương tự
    }

}
