package com.nhom7.quiz.quizapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.io.IOException;
import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.nhom7.quiz.quizapp.model.Result;
import com.nhom7.quiz.quizapp.service.QuizService;
import com.nhom7.quiz.quizapp.service.ExcelImportService;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.service.ResultService;
import com.nhom7.quiz.quizapp.model.dto.QuizImportDto;
import com.nhom7.quiz.quizapp.model.dto.QuizDetailDTO;
import com.nhom7.quiz.quizapp.config.JwtUtil;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

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

	@Autowired
	private ResultService resultService;

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
			// DEBUG: In ra thông tin nhận được
			System.out.println("Received - Title: " + title);
			System.out.println("Received - Description: " + description);
			System.out.println("Received - IsPublic: " + isPublic);
			System.out.println("Received - CategoryId: " + categoryId);
			System.out.println("Received - UserId: " + userId);

			// TẠO QUIZ OBJECT TỪ PARAMETERS
			Quiz quiz = new Quiz();
			quiz.setTitle(title);
			quiz.setPublic(isPublic);

			// TÌM CATEGORY
			Category category = categoryRepo.findById(categoryId).orElse(null);
			if (category == null) {
				return new ResponseEntity<>("Category not found", HttpStatus.BAD_REQUEST);
			}
			quiz.setCategory(category);

			// TÌM USER
			User user = loginService.findById(userId);
			if (user == null) {
				return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
			}
			quiz.setUser(user);

			// TẠO QUIZ
			Quiz savedQuiz = quizService.createQuiz(quiz);

			// UPLOAD IMAGE NẾU CÓ
			if (imageFile != null && !imageFile.isEmpty()) {
				try {
					quizService.uploadImageForQuiz(savedQuiz, imageFile);
				} catch (IOException e) {
					e.printStackTrace();
					return new ResponseEntity<>("Lỗi khi upload ảnh: " + e.getMessage(),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}

			// TRẢ VỀ RESPONSE VỚI QUIZ CODE
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", "Tạo quiz thành công!");
			response.put("quiz", savedQuiz);
			response.put("quizCode", savedQuiz.getQuizCode());

			return new ResponseEntity<>(response, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Lỗi khi tạo quiz với ảnh: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/create-quiz")
	@PreAuthorize("hasAnyRole('USER','ADMIN')") // phải đăng nhập để tạo quiz
	public ResponseEntity<Quiz> createQuiz(@RequestBody Quiz quiz, Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		User owner = loginService.findByUsername(authentication.getName());
		if (owner == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		quiz.setUser(owner); // <— GÁN CHỦ SỞ HỮU
		Quiz saved = quizService.createQuiz(quiz); // gọi service/repo để lưu
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
		return quizService.getQuizById(id)
				.map(quiz -> ResponseEntity.ok().body(quiz))
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/detail/{id}")
	public ResponseEntity<QuizDetailDTO> getQuizDetail(@PathVariable Long id) {
		System.out.println("Requesting quiz detail for ID: " + id);
		try {
			Optional<QuizDetailDTO> detail = quizService.getQuizDetail(id);
			if (detail.isPresent()) {
				System.out.println("Quiz detail found: " + detail.get().getTitle());
				return ResponseEntity.ok().body(detail.get());
			} else {
				System.out.println("Quiz not found for ID: " + id);
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			System.err.println("Error getting quiz detail: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @quizService.isOwner(#id, authentication.name)")
	public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
		System.out.println("[QuizController] updateQuiz called for ID: " + id);
		System.out.println("[QuizController] Request body quiz isPublic: " + quiz.isPublic());

		try {
			Optional<Quiz> result = quizService.updateQuiz(id, quiz);
			if (result.isPresent()) {
				Quiz updatedQuiz = result.get();
				System.out.println(
						"[QuizController] Quiz updated successfully - Final isPublic: " + updatedQuiz.isPublic());
				return ResponseEntity.ok().body(updatedQuiz);
			} else {
				System.out.println("[QuizController] Quiz not found for ID: " + id);
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			System.err.println("[QuizController] Error updating quiz: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @quizService.isOwner(#id, authentication.name)")
	public ResponseEntity<Map<String, Object>> deleteQuiz(@PathVariable Long id, HttpServletRequest request) {
		try {
			boolean deleted = quizService.deleteQuiz(id);
			if (deleted) {
				Map<String, Object> response = new HashMap<>();
				response.put("success", true);
				response.put("message", "Quiz đã được xóa thành công");
				return ResponseEntity.ok(response);
			} else {
				Map<String, Object> response = new HashMap<>();
				response.put("success", false);
				response.put("message", "Không tìm thấy quiz hoặc đã bị xóa");
				return ResponseEntity.ok(response);
			}
		} catch (Exception e) {
			Map<String, Object> response = new HashMap<>();
			response.put("success", false);
			response.put("message", "Lỗi khi xóa quiz: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// DEBUG ENDPOINT - XÓA SAU KHI TEST XONG
	@GetMapping("/debug/my-quizzes")
	public ResponseEntity<List<Map<String, Object>>> debugMyQuizzes() {
		List<Map<String, Object>> result = new ArrayList<>();

		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				String username = auth.getName();
				User user = loginService.findByUsername(username);
				if (user != null) {
					List<Quiz> allQuizzes = quizService.getAllQuiz();
					for (Quiz quiz : allQuizzes) {
						if (quiz.getUser() != null && quiz.getUser().getId().equals(user.getId())) {
							Map<String, Object> quizInfo = new HashMap<>();
							quizInfo.put("id", quiz.getId());
							quizInfo.put("title", quiz.getTitle());
							quizInfo.put("deleted", quiz.isDeleted());
							quizInfo.put("owner", quiz.getUser().getUsername());
							result.add(quizInfo);
						}
					}
				}
			}
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(new ArrayList<>());
		}
	}

	@GetMapping("/debug/{id}")
	public ResponseEntity<Map<String, Object>> debugQuiz(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Quiz> quizOpt = quizService.getQuizById(id);
			if (quizOpt.isPresent()) {
				Quiz quiz = quizOpt.get();
				response.put("found", true);
				response.put("id", quiz.getId());
				response.put("title", quiz.getTitle());
				response.put("deleted", quiz.isDeleted());
				response.put("owner", quiz.getUser() != null ? quiz.getUser().getUsername() : "NULL");
				response.put("ownerId", quiz.getUser() != null ? quiz.getUser().getId() : "NULL");
			} else {
				response.put("found", false);
			}

			// Kiểm tra authentication
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				response.put("currentUser", auth.getName());
				response.put("authorities", auth.getAuthorities().toString());
			}

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			response.put("error", e.getMessage());
			return ResponseEntity.status(500).body(response);
		}
	}

	// THÊM CÁC ENDPOINT MỚI CHO SOFT DELETE
	@DeleteMapping("/{id}/hard")
	public ResponseEntity<Map<String, Object>> hardDeleteQuiz(@PathVariable Long id, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();

		System.out.println("=== HARD DELETE DEBUG START ===");
		System.out.println("Attempting to hard delete quiz ID: " + id);

		// Kiểm tra authentication ngay đầu
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			System.out.println("No authentication found!");
			response.put("success", false);
			response.put("message", "Không có thông tin xác thực");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
		}

		try {
			System.out.println("Authentication name: " + auth.getName());
			System.out.println("Authentication authorities: " + auth.getAuthorities());
			System.out.println("Authentication principal: " + auth.getPrincipal());

			// Check if user is admin
			boolean isAdmin = auth.getAuthorities().stream()
					.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
			System.out.println("Is Admin: " + isAdmin);

			// Check if user owns the quiz
			System.out.println("About to call isOwner with ID: " + id + " and username: " + auth.getName());
			boolean isOwner = quizService.isOwner(id, auth.getName());
			System.out.println("Is Owner: " + isOwner);

			// If not admin and not owner, deny access
			if (!isAdmin && !isOwner) {
				System.out.println("Access denied - not admin and not owner");
				response.put("success", false);
				response.put("message", "Bạn không có quyền xóa quiz này");
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
			}

			System.out.println("Authorization passed, proceeding with deletion...");

			boolean deleteResult = quizService.hardDeleteQuiz(id);
			System.out.println("Hard delete result: " + deleteResult);

			if (deleteResult) {
				response.put("success", true);
				response.put("message", "Quiz đã được xóa hoàn toàn");
				System.out.println("Quiz hard deleted successfully");
				return ResponseEntity.ok(response);
			} else {
				response.put("success", false);
				response.put("message", "Quiz không tồn tại");
				System.out.println("Quiz not found");
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			System.err.println("Error hard deleting quiz: " + e.getMessage());
			response.put("success", false);
			response.put("message", "Lỗi khi xóa quiz: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@PostMapping("/{id}/restore")
	@PreAuthorize("hasRole('ADMIN') or @quizService.isOwner(#id, authentication.name)")
	public ResponseEntity<Map<String, Object>> restoreQuiz(@PathVariable Long id, HttpServletRequest request) {
		Map<String, Object> response = new HashMap<>();

		try {
			System.out.println("Attempting to restore quiz ID: " + id);
			boolean restoreResult = quizService.restoreQuiz(id);
			System.out.println("Restore result: " + restoreResult);

			if (restoreResult) {
				response.put("success", true);
				response.put("message", "Quiz đã được khôi phục thành công");
				System.out.println("Quiz restored successfully");
				return ResponseEntity.ok(response);
			} else {
				response.put("success", false);
				response.put("message", "Quiz không tồn tại");
				System.out.println("Quiz not found");
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			System.err.println("Error restoring quiz: " + e.getMessage());
			response.put("success", false);
			response.put("message", "Lỗi khi khôi phục quiz: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/user/{userId}/deleted/paginated")
	public ResponseEntity<Map<String, Object>> getDeletedQuizzesByUser(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) {

		Page<Quiz> quizPage = quizService.getDeletedQuizzesByUserPaginated(userId, page, size);
		List<Quiz> quizzes = quizPage.getContent();

		Map<String, Object> response = new HashMap<>();
		response.put("quizzes", quizzes);
		response.put("currentPage", quizPage.getNumber());
		response.put("totalPages", quizPage.getTotalPages());
		response.put("totalItems", quizPage.getTotalElements());
		response.put("pageSize", quizPage.getSize());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/user/{userId}/paginated")
	public ResponseEntity<Map<String, Object>> getPaginatedQuizzesByUser(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "6") int size) {

		Page<Quiz> quizPage = quizService.getQuizzesByUserPaginated(userId, page, size);
		List<Quiz> quizzes = quizPage.getContent();

		// DEBUG: Kiểm tra trạng thái isPublic của quiz
		System.out.println("Debug: Checking quiz public status for user " + userId);
		for (Quiz quiz : quizzes) {
			System.out.println("Quiz ID: " + quiz.getId() +
					", Title: " + quiz.getTitle() +
					", IsPublic: " + quiz.isPublic() +
					", Deleted: " + quiz.isDeleted() +
					", Has Image: " + (quiz.getImage() != null));
		}

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

		Page<Quiz> quizPage = quizService.getPublicQuizzes(Boolean.TRUE, page, size);

		// DEBUG: Kiểm tra public quiz
		System.out.println("Debug: Checking public quizzes");
		for (Quiz quiz : quizPage.getContent()) {
			System.out.println("Public Quiz ID: " + quiz.getId() +
					", Title: " + quiz.getTitle() +
					", IsPublic: " + quiz.isPublic() +
					", Deleted: " + quiz.isDeleted() +
					", Has Image: " + (quiz.getImage() != null));
		}

		return quizPage;
	}

	// THÊM ENDPOINT PREVIEW
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
				// Bỏ gửi điểm câu hỏi
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

	// THÊM ENDPOINT MỚI CHO IMPORT VỚI IMAGE
	@PostMapping(value = "/import-excel-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Map<String, Object>> importQuizWithImage(
			@RequestParam("file") MultipartFile excelFile,
			@RequestParam("title") String title,
			@RequestParam("description") String description,
			@RequestParam("categoryId") Long categoryId,
			@RequestParam("username") String username,
			@RequestParam("isPublic") boolean isPublic, // THÊM ISPUBLIC PARAMETER
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
			response.put("quizCode", importedQuiz.getQuizCode());
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
				"Đáp án đúng", "Thời gian (giây)" });
		response.put("notes", "Đáp án đúng phải là A, B, C, hoặc D. Thời gian mặc định 30s, range 5-300s.");
		return ResponseEntity.ok(response);
	}

	// THÊM ENDPOINT JOIN QUIZ BẰNG CODE
	@GetMapping("/join/{code}")
	public ResponseEntity<Map<String, Object>> joinQuizByCode(@PathVariable String code) {
		Map<String, Object> response = new HashMap<>();

		try {
			System.out.println("Join quiz request with code: " + code);

			Optional<Quiz> quizOpt = quizService.findByQuizCode(code);

			if (quizOpt.isEmpty()) {
				System.out.println("Quiz not found for code: " + code);
				response.put("success", false);
				response.put("message", "Không tìm thấy quiz với mã code này");
				return ResponseEntity.notFound().build();
			}

			Quiz quiz = quizOpt.get();

			if (quiz.isDeleted() != null && quiz.isDeleted()) {
				System.out.println("Quiz is deleted: " + quiz.getId());
				response.put("success", false);
				response.put("message", "Quiz này đã bị xóa");
				return ResponseEntity.badRequest().body(response);
			}

			System.out.println("Quiz found: " + quiz.getTitle());

			// TRẢ VỀ THÔNG TIN QUIZ ĐỂ PREVIEW
			Map<String, Object> quizInfo = new HashMap<>();
			quizInfo.put("quizId", quiz.getId());
			quizInfo.put("title", quiz.getTitle());
			quizInfo.put("quizCode", quiz.getQuizCode());
			quizInfo.put("isPublic", quiz.isPublic());
			quizInfo.put("createdAt", quiz.getCreatedAt());

			// Thêm thông tin category nếu có
			if (quiz.getCategory() != null) {
				quizInfo.put("categoryName", quiz.getCategory().getName());
			}

			// Thêm thông tin user nếu có
			if (quiz.getUser() != null) {
				quizInfo.put("creatorName", quiz.getUser().getFullName());
			}

			response.put("success", true);
			response.put("message", "Tìm thấy quiz thành công");
			response.put("quiz", quizInfo);

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("Error joining quiz: " + e.getMessage());
			response.put("success", false);
			response.put("message", "Lỗi khi tham gia quiz: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// THÊM ENDPOINT LẤY QUIZ CODE
	@GetMapping("/{id}/code")
	public ResponseEntity<Map<String, Object>> getQuizCode(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();

		try {
			Optional<Quiz> quizOpt = quizService.getQuizById(id);

			if (quizOpt.isEmpty()) {
				response.put("success", false);
				response.put("message", "Quiz không tồn tại");
				return ResponseEntity.notFound().build();
			}

			Quiz quiz = quizOpt.get();

			// Kiểm tra quiz có bị xóa không
			if (Boolean.TRUE.equals(quiz.isDeleted())) {
				response.put("success", false);
				response.put("message", "Quiz đã bị xóa");
				return ResponseEntity.ok(response);
			}

			// Tạo code nếu chưa có
			if (quiz.getQuizCode() == null || quiz.getQuizCode().isEmpty()) {
				String quizCode = quizService.generateQuizCode(quiz.getId());
				quiz.setQuizCode(quizCode);
				quiz.setCodeCreatedAt(LocalDateTime.now());
				quiz = quizService.updateQuiz(quiz.getId(), quiz).orElse(quiz);
			}

			response.put("success", true);
			response.put("quizCode", quiz.getQuizCode());
			response.put("quizTitle", quiz.getTitle());
			response.put("codeCreatedAt", quiz.getCodeCreatedAt());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("Error getting quiz code: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Lỗi khi lấy mã code: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/debug/user/{userId}")
	public ResponseEntity<Map<String, Object>> debugUserQuizzes(@PathVariable Long userId) {
		Map<String, Object> response = new HashMap<>();

		try {
			// Lấy tất cả quiz của user (kể cả đã xóa)
			Page<Quiz> allQuizzes = quizService.getAllQuizzesByUserPaginated(userId, 0, 100);

			// Lấy quiz chưa bị xóa
			Page<Quiz> activeQuizzes = quizService.getQuizzesByUserPaginated(userId, 0, 100);

			// Lấy quiz đã bị xóa
			Page<Quiz> deletedQuizzes = quizService.getDeletedQuizzesByUserPaginated(userId, 0, 100);

			response.put("allQuizzesCount", allQuizzes.getTotalElements());
			response.put("activeQuizzesCount", activeQuizzes.getTotalElements());
			response.put("deletedQuizzesCount", deletedQuizzes.getTotalElements());

			response.put("allQuizzes", allQuizzes.getContent());
			response.put("activeQuizzes", activeQuizzes.getContent());
			response.put("deletedQuizzes", deletedQuizzes.getContent());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("Error debugging user quizzes: " + e.getMessage());
			response.put("error", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	// THÊM ENDPOINT LẤY QUIZ PUBLIC THEO CATEGORY
	@GetMapping("/public/category/{categoryId}")
	public ResponseEntity<Map<String, Object>> getPublicQuizzesByCategory(
			@PathVariable Long categoryId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "8") int size) {

		Map<String, Object> response = new HashMap<>();

		try {
			System.out.println("Requesting public quizzes for category ID: " + categoryId);

			// Kiểm tra category có tồn tại không
			Optional<Category> categoryOpt = categoryRepo.findById(categoryId);
			if (categoryOpt.isEmpty()) {
				System.out.println("Category not found for ID: " + categoryId);
				response.put("success", false);
				response.put("message", "Không tìm thấy danh mục này");
				return ResponseEntity.notFound().build();
			}

			Category category = categoryOpt.get();
			System.out.println("Category found: " + category.getName());

			// Lấy quiz public theo category
			Page<Quiz> quizPage = quizService.getPublicQuizzesByCategory(categoryId, page, size);
			List<Quiz> quizzes = quizPage.getContent();

			// DEBUG: Kiểm tra quiz public theo category
			System.out.println("Debug: Checking public quizzes for category " + category.getName());
			for (Quiz quiz : quizzes) {
				System.out.println("Public Quiz ID: " + quiz.getId() +
						", Title: " + quiz.getTitle() +
						", IsPublic: " + quiz.isPublic() +
						", Category: " + (quiz.getCategory() != null ? quiz.getCategory().getName() : "NULL") +
						", Deleted: " + quiz.isDeleted() +
						", Has Image: " + (quiz.getImage() != null));
			}

			response.put("success", true);
			response.put("message", "Lấy danh sách quiz công khai theo danh mục thành công");
			response.put("quizzes", quizzes);
			response.put("category", Map.of(
					"id", category.getId(),
					"name", category.getName()));
			response.put("currentPage", quizPage.getNumber());
			response.put("totalPages", quizPage.getTotalPages());
			response.put("totalItems", quizPage.getTotalElements());
			response.put("pageSize", quizPage.getSize());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			System.err.println("Error getting public quizzes by category: " + e.getMessage());
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Lỗi khi lấy danh sách quiz: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	@GetMapping("/public/stats/{id}")
	public ResponseEntity<Map<String, Object>> getPublicQuizStats(@PathVariable Long id) {
		System.out.println("Requesting public quiz stats for ID: " + id);
		try {
			Optional<Quiz> quizOpt = quizService.getQuizById(id);
			if (quizOpt.isEmpty()) {
				System.out.println("Quiz not found for ID: " + id);
				return ResponseEntity.notFound().build();
			}

			Quiz quiz = quizOpt.get();

			// Chỉ trả về thống kê cho quiz công khai
			if (!quiz.isPublic()) {
				System.out.println("Quiz is private, cannot show stats");
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
						.body(Map.of("error", "Quiz này là riêng tư"));
			}

			// Lấy thống kê cơ bản
			Map<String, Object> stats = new HashMap<>();
			stats.put("totalQuestions", quiz.getQuestions().size());
			// Bỏ thống kê tổng điểm câu hỏi
			stats.put("totalPoints", 0);
			stats.put("totalTime", quiz.getQuestions().stream()
					.mapToInt(q -> q.getTimeLimit()).sum());

			// Lấy thống kê từ kết quả (nếu có)
			try {
				List<Result> quizResults = resultService.getResultsByQuizIdPublic(id);
				int totalPlays = (quizResults != null) ? quizResults.size() : 0;
				stats.put("totalPlays", totalPlays);

				if (totalPlays > 0 && quizResults != null) {
					double avgScore = quizResults.stream()
							.mapToInt(r -> r.getScore())
							.average().orElse(0.0);
					stats.put("averageScore", Math.round(avgScore * 10.0) / 10.0);

					long unique = quizResults.stream()
							.map(r -> r.getUser().getId())
							.distinct().count();
					stats.put("uniqueParticipants", (int) unique);

					long completed = quizResults.stream()
							.filter(r -> r.getScore() > 0)
							.count();
					stats.put("completionRate", totalPlays > 0 ? (completed * 100.0 / totalPlays) : 0.0);

					double avgTime = quizResults.stream()
							.filter(r -> r.getTimeTaken() != null)
							.mapToInt(r -> r.getTimeTaken())
							.average().orElse(0.0);
					stats.put("averageTime", (int) Math.round(avgTime));
				} else {
					stats.put("averageScore", 0.0);
					stats.put("uniqueParticipants", 0);
					stats.put("completionRate", 0.0);
					stats.put("averageTime", 0);
				}
			} catch (Exception ignore) {
				stats.put("totalPlays", 0);
				stats.put("averageScore", 0.0);
				stats.put("uniqueParticipants", 0);
				stats.put("completionRate", 0.0);
				stats.put("averageTime", 0);
			}

			System.out.println("Public quiz stats: " + stats);
			return ResponseEntity.ok(stats);

		} catch (Exception e) {
			System.err.println("Error getting public quiz stats: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
