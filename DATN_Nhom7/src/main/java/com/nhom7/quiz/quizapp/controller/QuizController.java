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
			@RequestParam(defaultValue = "8") int size) {
		return quizService.getPublicQuizzes(true, page, size);
	}

	@PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> importQuizFromExcel(
			@RequestParam("file") MultipartFile file,
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam("username") String username) {
		
		Map<String, Object> response = new HashMap<>();
		
		try {
			// Validate file
			if (file.isEmpty()) {
				response.put("success", false);
				response.put("message", "File không được để trống");
				return ResponseEntity.badRequest().body(response);
			}
			
			String fileName = file.getOriginalFilename();
			if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
				response.put("success", false);
				response.put("message", "File phải có định dạng .xlsx hoặc .xls");
				return ResponseEntity.badRequest().body(response);
			}
			
			// Import quiz from Excel
			Quiz importedQuiz = quizService.importQuizFromExcel(file, title, description, categoryId, username);
			
			response.put("success", true);
			response.put("message", "Import quiz thành công!");
			response.put("quiz", importedQuiz);
			response.put("questionsCount", importedQuiz.getQuestions() != null ? importedQuiz.getQuestions().size() : 0);
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Lỗi khi import: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/excel-template")
	public ResponseEntity<Map<String, Object>> getExcelTemplateInfo() {
		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("message", "Hướng dẫn sử dụng template Excel");
		response.put("columns", new String[]{"STT", "Câu hỏi", "Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D", "Đáp án đúng", "Điểm"});
		response.put("notes", "Đáp án đúng phải là A, B, C, hoặc D. Điểm phải là số nguyên dương.");
		return ResponseEntity.ok(response);
	}

}
