package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.service.QuizService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {
	@Autowired
	private QuizService quizService;

	@GetMapping
	public List<Quiz> getAllQuizzes() {
		return quizService.getAllQuiz();
	}

	@PostMapping("/create-quiz")
	public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz) {
		return new ResponseEntity<>(quizService.createQuiz(quiz), HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
		return quizService.getQuizById(id)
				.map(quiz -> ResponseEntity.ok().body(quiz))
				.orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
		return quizService.updateQuiz(id, quiz)
				.map(updatedQuiz -> ResponseEntity.ok().body(updatedQuiz))
				.orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
		if (quizService.deleteQuiz(id)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}

}
