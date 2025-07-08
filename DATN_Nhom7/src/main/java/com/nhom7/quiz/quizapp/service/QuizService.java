package com.nhom7.quiz.quizapp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

	// Lấy danh sách quiz theo userId (không phân trang)
	// public List<Quiz> getQuizzesByUserId(Long userId) {
	// return quizRepo.findByUserId(userId);
	// }

	// Lấy danh sách quiz theo userId có phân trang
	public Page<Quiz> getQuizzesByUserPaginated(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return quizRepo.findByUserId(userId, pageable);
	}

	public Page<Quiz> getPublicQuizzes(boolean isPublic, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return quizRepo.findByIsPublic(isPublic, pageable);
	}

}
