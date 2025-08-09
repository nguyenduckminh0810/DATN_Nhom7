package com.nhom7.quiz.quizapp.model.dto;

import java.util.List;

public class EvaluationResult {
    private Long resultId;
    private int score;
    private List<CorrectAnswerDTO> correctAnswers;

    public EvaluationResult(Long resultId, int score, List<CorrectAnswerDTO> correctAnswers) {
        this.resultId = resultId;
        this.score = score;
        this.correctAnswers = correctAnswers;
    }

    public Long getResultId() { return resultId; }
    public int getScore() { return score; }
    public List<CorrectAnswerDTO> getCorrectAnswers() { return correctAnswers; }
}
