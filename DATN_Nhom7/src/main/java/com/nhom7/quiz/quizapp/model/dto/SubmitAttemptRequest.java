package com.nhom7.quiz.quizapp.model.dto;

import java.util.List;

public class SubmitAttemptRequest {
    private List<QuizSubmissionDTO.AnswerSubmission> answers;
    private Integer timeTaken; // optional, seconds

    public List<QuizSubmissionDTO.AnswerSubmission> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizSubmissionDTO.AnswerSubmission> answers) {
        this.answers = answers;
    }

    public Integer getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Integer timeTaken) {
        this.timeTaken = timeTaken;
    }
}


