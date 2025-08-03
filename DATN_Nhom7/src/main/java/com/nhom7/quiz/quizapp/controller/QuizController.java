package com.nhom7.quiz.quizapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.io.IOException;

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
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.service.QuizService;
import com.nhom7.quiz.quizapp.service.ExcelImportService;
import com.nhom7.quiz.quizapp.model.dto.QuizImportDto;
import com.nhom7.quiz.quizapp.model.dto.QuizDetailDTO;
import com.nhom7.quiz.quizapp.config.JwtUtil;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {
	@Autowired
	private QuizService quizService;

	@Autowired
	private ExcelImportService excelImportService;

	@Autowired
	private LoginService loginService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private CategoryRepo categoryRepo;

	@GetMapping
	public List<Quiz> getAllQuizzes() {
		return quizService.getAllQuiz();
	}

	@PostMapping(value = "/create-quiz-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> createQuizWithImage(
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("isPublic") boolean isPublic,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam("userId") Long userId,
			@RequestParam(value = "image", required = false) MultipartFile imageFile,
			HttpServletRequest request) {

		try {
			// ✅ DEBUG: In ra thông tin nhận được
			System.out.println("📝 Received - Title: " + title);
			System.out.println("📝 Received - Description: " + description);
			System.out.println("📝 Received - IsPublic: " + isPublic);
			System.out.println("📝 Received - CategoryId: " + categoryId);
			System.out.println("📝 Received - UserId: " + userId);

			// ✅ TẠO QUIZ OBJECT TỪ PARAMETERS
			Quiz quiz = new Quiz();
			quiz.setTitle(title);
			quiz.setPublic(isPublic);

			// ✅ TÌM CATEGORY
			Category category = categoryRepo.findById(categoryId).orElse(null);
			if (category == null) {
				return new ResponseEntity<>("Category not found", HttpStatus.BAD_REQUEST);
			}
			quiz.setCategory(category);

			// ✅ TÌM USER
			User user = loginService.findById(userId);
			if (user == null) {
				return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
			}
			quiz.setUser(user);

			// ✅ TẠO QUIZ
			Quiz savedQuiz = quizService.createQuiz(quiz);

			// ✅ UPLOAD IMAGE NẾU CÓ
			if (imageFile != null && !imageFile.isEmpty()) {
				try {
					quizService.uploadImageForQuiz(savedQuiz, imageFile);
				} catch (IOException e) {
					e.printStackTrace();
					return new ResponseEntity<>("Lỗi khi upload ảnh: " + e.getMessage(),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

			return new ResponseEntity<>(savedQuiz, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Lỗi khi tạo quiz với ảnh: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
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

	@GetMapping("/{id}/detail")
	public ResponseEntity<QuizDetailDTO> getQuizDetail(@PathVariable Long id) {
		System.out.println("🔍 Requesting quiz detail for ID: " + id);
		try {
			Optional<QuizDetailDTO> detail = quizService.getQuizDetail(id);
			if (detail.isPresent()) {
				System.out.println("✅ Quiz detail found: " + detail.get().getTitle());
				return ResponseEntity.ok().body(detail.get());
			} else {
				System.out.println("❌ Quiz not found for ID: " + id);
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			System.err.println("❌ Error getting quiz detail: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
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

	// ✅ THÊM ENDPOINT PREVIEW
	@PostMapping(value = "/preview-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> previewExcelFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("categoryId") Long categoryId) {

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

			// Preview Excel file
			QuizImportDto quizData = excelImportService.previewExcelFile(file, title, description, categoryId);

			// Tạo response preview
			response.put("success", true);
			response.put("message", "Preview thành công!");
			response.put("quizData", quizData);
			response.put("totalQuestions", quizData.getQuestions().size());
			response.put("topicImageUrl", quizData.getTopicImageUrl());

			// Preview 3 câu hỏi đầu tiên
			List<Map<String, Object>> previewQuestions = new ArrayList<>();
			for (int i = 0; i < Math.min(3, quizData.getQuestions().size()); i++) {
				var question = quizData.getQuestions().get(i);
				Map<String, Object> questionPreview = new HashMap<>();
				questionPreview.put("content", question.getContent());
				questionPreview.put("point", question.getPoint());
				questionPreview.put("timeLimit", question.getTimeLimit());
				questionPreview.put("answers", question.getAnswers());
				previewQuestions.add(questionPreview);
			}
			response.put("previewQuestions", previewQuestions);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Lỗi khi preview: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> importQuizFromExcel(
			@RequestParam("file") MultipartFile file,
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam("username") String username,
			@RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic) {

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

			// Import quiz from Excel with isPublic parameter
			Quiz importedQuiz = quizService.importQuizFromExcel(file, title, description, categoryId, username,
					isPublic);

			response.put("success", true);
			response.put("message", "Import quiz thành công!");
			response.put("quiz", importedQuiz);
			response.put("questionsCount",
					importedQuiz.getQuestions() != null ? importedQuiz.getQuestions().size() : 0);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Lỗi khi import: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// ✅ THÊM ENDPOINT MỚI CHO IMPORT VỚI IMAGE
	@PostMapping(value = "/import-excel-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> importQuizWithImage(
			@RequestParam("file") MultipartFile excelFile,
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam("username") String username,
			@RequestParam("isPublic") boolean isPublic, // ✅ THÊM ISPUBLIC PARAMETER
			@RequestParam(value = "image", required = false) MultipartFile imageFile) {

		Map<String, Object> response = new HashMap<>();

		try {
			// Validate Excel file
			if (excelFile.isEmpty()) {
				response.put("success", false);
				response.put("message", "File Excel không được để trống");
				return ResponseEntity.badRequest().body(response);
			}

			String fileName = excelFile.getOriginalFilename();
			if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
				response.put("success", false);
				response.put("message", "File phải có định dạng .xlsx hoặc .xls");
				return ResponseEntity.badRequest().body(response);
			}

			// Validate image file if provided
			if (imageFile != null && !imageFile.isEmpty()) {
				if (!imageFile.getContentType().startsWith("image/")) {
					response.put("success", false);
					response.put("message", "File ảnh không hợp lệ. Chỉ chấp nhận file ảnh.");
					return ResponseEntity.badRequest().body(response);
				}

				// Check file size (max 5MB)
				if (imageFile.getSize() > 5 * 1024 * 1024) {
					response.put("success", false);
					response.put("message", "File ảnh quá lớn. Tối đa 5MB.");
					return ResponseEntity.badRequest().body(response);
				}
			}

			// Import quiz from Excel with isPublic parameter
			Quiz importedQuiz = quizService.importQuizFromExcel(excelFile, title, description, categoryId, username,
					isPublic);

			// Upload image if provided
			if (imageFile != null && !imageFile.isEmpty()) {
				try {
					quizService.uploadImageForQuiz(importedQuiz, imageFile);
				} catch (IOException e) {
					e.printStackTrace();
					response.put("success", false);
					response.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
				}
			}

			response.put("success", true);
			response.put("message", "Import quiz thành công!");
			response.put("quiz", importedQuiz);
			response.put("questionsCount",
					importedQuiz.getQuestions() != null ? importedQuiz.getQuestions().size() : 0);
			response.put("hasImage", imageFile != null && !imageFile.isEmpty());

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
		response.put("columns", new String[] { "STT", "Câu hỏi", "Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D",
				"Đáp án đúng", "Thời gian (giây)" }); // ✅ THÊM CỘT THỜI GIAN
		response.put("notes", "Đáp án đúng phải là A, B, C, hoặc D. Thời gian mặc định 30s, range 5-300s.");
		return ResponseEntity.ok(response);
	}

}
