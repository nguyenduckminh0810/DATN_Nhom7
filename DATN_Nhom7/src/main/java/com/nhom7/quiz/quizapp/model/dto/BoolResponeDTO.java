package com.nhom7.quiz.quizapp.model.dto;

public class BoolResponeDTO {
    private boolean valid;

    public BoolResponeDTO(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
