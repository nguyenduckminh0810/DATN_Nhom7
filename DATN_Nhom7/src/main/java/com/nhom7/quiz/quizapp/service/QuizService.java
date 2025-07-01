package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.repository.QuizRepo;

@Service
public class QuizService {
	@Autowired
	private QuizRepo quizRepo;

	// Lấy tất cả quiz
	public List<Quiz> getAllQuiz() {
		return quizRepo.findAll();
	}

	// Tạo quiz mới
	public Quiz createQuiz(Quiz quiz) {
		quiz.setCreatedAt(LocalDateTime.now());
		return quizRepo.save(quiz);
	}

	// Lấy quiz theo ID
	public Optional<Quiz> getQuizById(Long id) {
		return quizRepo.findById(id);
	}

	// Cập nhật quiz
	public Optional<Quiz> updateQuiz(Long id, Quiz updatedQuiz) {
		return quizRepo.findById(id).map(quiz -> {
			quiz.setTitle(updatedQuiz.getTitle());
			quiz.setPublic(updatedQuiz.isPublic());
			quiz.setCategory(updatedQuiz.getCategory());
			// bạn có thể set thêm các trường khác như tags nếu cần
			return quizRepo.save(quiz);
		});
	}

	// Xoá quiz
	public boolean deleteQuiz(Long id) {
		if (quizRepo.existsById(id)) {
			quizRepo.deleteById(id);
			return true;
		}
		return false;
	}
}
