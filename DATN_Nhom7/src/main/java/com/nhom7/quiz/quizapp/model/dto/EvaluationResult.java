package com.nhom7.quiz.quizapp.model.dto;

import java.util.List;

public class EvaluationResult {
    private int score;
    private List<CorrectAnswerDTO> correctAnswers;

    public EvaluationResult(int score, List<CorrectAnswerDTO> correctAnswers) {
        this.score = score;
        this.correctAnswers = correctAnswers;
    }

    public int getScore() {
        return score;
    }

    public List<CorrectAnswerDTO> getCorrectAnswers() {
        return correctAnswers;
    }
}
