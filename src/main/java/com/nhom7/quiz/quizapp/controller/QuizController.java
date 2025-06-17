package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.service.QuizService;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/quiz")
public class QuizController {
	@Autowired
	private QuizService quizService;
	
	@GetMapping("/all-quiz")
	public List<Quiz> getAllQuiz(){
		return quizService.getAllQuiz();
	}
	
}
