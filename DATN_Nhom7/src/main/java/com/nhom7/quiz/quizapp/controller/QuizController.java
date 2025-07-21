package com.nhom7.quiz.quizapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
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

	@PostMapping(value = "/create-quiz-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> createQuizWithImage(
			@RequestPart("quiz") String quizJson,
			@RequestPart("image") MultipartFile imageFile) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			Quiz quiz = mapper.readValue(quizJson, Quiz.class); // Parse JSON quiz

			Quiz savedQuiz = quizService.createQuiz(quiz); // Lưu quiz trước

			if (!imageFile.isEmpty()) {
				quizService.uploadImageForQuiz(savedQuiz, imageFile); // Lưu ảnh liên kết với quiz
			}

			return new ResponseEntity<>(savedQuiz, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Lỗi khi tạo quiz với ảnh", HttpStatus.INTERNAL_SERVER_ERROR);
		}
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
		return quizService.deleteQuiz(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}

	@GetMapping("/user/{userId}/paginated")
	public ResponseEntity<Map<String, Object>> getPaginatedQuizzesByUser(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) {

		Page<Quiz> quizPage = quizService.getQuizzesByUserPaginated(userId, page, size);
		List<Quiz> quizzes = quizPage.getContent();

		Map<String, Object> response = new HashMap<>();
		response.put("quizzes", quizzes);
		response.put("currentPage", quizPage.getNumber());
		response.put("totalPages", quizPage.getTotalPages());
		response.put("totalItems", quizPage.getTotalElements());
		response.put("pageSize", quizPage.getSize());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/public")
	public Page<Quiz> getPublicQuizzes(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) {
		return quizService.getPublicQuizzes(true, page, size);
	}

}
