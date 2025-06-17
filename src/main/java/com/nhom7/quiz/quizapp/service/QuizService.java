package com.nhom7.quiz.quizapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.repository.QuizRepo;

@Service
public class QuizService {
	@Autowired
	private QuizRepo quizRepo;
	
	public List<Quiz> getAllQuiz(){
		return quizRepo.findAll();
	}
}
