package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.CorrectAnswerDTO;
import com.nhom7.quiz.quizapp.model.dto.EvaluationResult;
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

    public List<Result> getResultsByUserId(Long userId) {
        return resultRepo.findByUser_Id(userId);
    }

    public EvaluationResult evaluateAndSave(QuizSubmissionDTO submission) {
        int correctCount = 0;
        List<CorrectAnswerDTO> correctAnswers = new ArrayList<>();

        for (QuizSubmissionDTO.AnswerSubmission ans : submission.getAnswers()) {
            Long questionId = ans.getQuestionId();

            // Lấy đáp án đúng nhất cho câu hỏi
            Optional<Answer> correctAnswerOpt = answerRepo.findByQuestion_IdAndIsCorrectTrue(questionId);

            if (correctAnswerOpt.isPresent()) {
                Long correctAnswerId = correctAnswerOpt.get().getId();
                correctAnswers.add(new CorrectAnswerDTO(questionId, correctAnswerId));

                if (correctAnswerId.equals(ans.getAnswerId())) {
                    correctCount++;
                }
            } else {
                // Nếu không tìm thấy đáp án đúng thì vẫn thêm vào để client biết
                correctAnswers.add(new CorrectAnswerDTO(questionId, null));
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

        return new EvaluationResult(score, correctAnswers);
    }
}
