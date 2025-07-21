package com.nhom7.quiz.quizapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Image;
import com.nhom7.quiz.quizapp.repository.ImageRepo;

@Service
public class ImageService {

    @Autowired
    private ImageRepo imageRepo;

    public Image getImageByQuizId(Long quizId) {
        return imageRepo.findByQuizId(quizId);
    }

    public Image getImageByQuestionId(Long questionId) {
        return imageRepo.findByQuestionId(questionId);
    }

}
