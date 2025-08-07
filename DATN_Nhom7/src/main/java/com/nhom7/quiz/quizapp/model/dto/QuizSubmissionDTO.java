package com.nhom7.quiz.quizapp.model.dto;

import java.util.List;

public class QuizSubmissionDTO {
    private Long quizId;
    private Long userId;
    private List<AnswerSubmission> answers;
    private Integer timeTaken; // Thời gian làm quiz (giây)

    public QuizSubmissionDTO() {
    }

    public QuizSubmissionDTO(Long quizId, Long userId, List<AnswerSubmission> answers) {
        this.quizId = quizId;
        this.userId = userId;
        this.answers = answers;
    }

    public QuizSubmissionDTO(Long quizId, Long userId, List<AnswerSubmission> answers, Integer timeTaken) {
        this.quizId = quizId;
        this.userId = userId;
        this.answers = answers;
        this.timeTaken = timeTaken;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<AnswerSubmission> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmission> answers) {
        this.answers = answers;
    }

    public Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }

    public static class AnswerSubmission {
        private Long questionId;
        private Long answerId;

        public AnswerSubmission() {
        }

        public AnswerSubmission(Long questionId, Long answerId) {
            this.questionId = questionId;
            this.answerId = answerId;
        }

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Long getAnswerId() {
            return answerId;
        }

        public void setAnswerId(Long answerId) {
            this.answerId = answerId;
        }
    }
}
