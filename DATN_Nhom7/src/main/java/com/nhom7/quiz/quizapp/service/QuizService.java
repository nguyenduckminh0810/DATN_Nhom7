package com.nhom7.quiz.quizapp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nhom7.quiz.quizapp.model.Answer;
import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.model.Image;
import com.nhom7.quiz.quizapp.model.Question;
import com.nhom7.quiz.quizapp.model.Quiz;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.model.dto.AnswerImportDto;
import com.nhom7.quiz.quizapp.model.dto.QuestionImportDto;
import com.nhom7.quiz.quizapp.model.dto.QuizImportDto;
import com.nhom7.quiz.quizapp.model.dto.QuizDetailDTO;
import com.nhom7.quiz.quizapp.model.dto.QuizAttemptSummaryDTO;
import com.nhom7.quiz.quizapp.repository.CategoryRepo; // ✅ THÊM IMPORT
import com.nhom7.quiz.quizapp.repository.ImageRepo;
import com.nhom7.quiz.quizapp.repository.QuizRepo;
import com.nhom7.quiz.quizapp.service.userService.LoginService;

@Service
public class QuizService {

	@Autowired
	private QuizRepo quizRepo;

	@Autowired
	private ImageRepo imageRepo;

	// ✅ THÊM CÁC DEPENDENCIES CẦN THIẾT
	@Autowired
	private LoginService loginService;

	@Autowired
	private CategoryRepo categoryRepo; // ✅ SỬA: Dùng Repository thay vì Service

	@Autowired
	private ExcelImportService excelImportService;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private AnswerService answerService;
	
	@Autowired
	private ResultService resultService;

	// ✅ OWNERSHIP VALIDATION METHOD
	public boolean isOwner(Long quizId, String username) {
		try {
			Optional<Quiz> quizOpt = quizRepo.findById(quizId);
			if (quizOpt.isPresent()) {
				Quiz quiz = quizOpt.get();
				User user = loginService.findByUsername(username);
				return user != null && quiz.getUser() != null && 
					   user.getId().equals(quiz.getUser().getId());
			}
			return false;
		} catch (Exception e) {
			System.err.println("❌ Error checking ownership: " + e.getMessage());
			return false;
		}
	}

	// Lấy tất cả quiz
	public List<Quiz> getAllQuiz() {
		return quizRepo.findAll();
	}

	// Tạo quiz mới
	public Quiz createQuiz(Quiz quiz) {
		try {
			// ✅ DEBUG: In ra thông tin quiz trước khi lưu
			System.out.println("📝 Creating quiz - Title: " + quiz.getTitle());
			System.out.println("📝 Quiz User: " + (quiz.getUser() != null ? quiz.getUser().getId() : "NULL"));
			System.out
					.println("📝 Quiz Category: " + (quiz.getCategory() != null ? quiz.getCategory().getId() : "NULL"));
			System.out.println("📝 Quiz IsPublic: " + quiz.isPublic());

			quiz.setCreatedAt(LocalDateTime.now());
			
			// ✅ LƯU QUIZ TRƯỚC
			Quiz savedQuiz = quizRepo.save(quiz);
			
			// ✅ TẠO CODE SAU KHI ĐÃ CÓ ID
			String quizCode = generateQuizCode(savedQuiz.getId());
			savedQuiz.setQuizCode(quizCode);
			savedQuiz.setCodeCreatedAt(LocalDateTime.now());
			
			// ✅ LƯU LẠI VỚI CODE
			Quiz finalQuiz = quizRepo.save(savedQuiz);

			// ✅ DEBUG: In ra quiz sau khi lưu
			System.out.println("📝 Quiz created successfully with ID: " + finalQuiz.getId());
			System.out.println("📝 Quiz Code: " + finalQuiz.getQuizCode());

			return finalQuiz;
		} catch (Exception e) {
			System.err.println("❌ Error creating quiz: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	// ✅ THÊM METHOD TẠO CODE QUIZ
	public String generateQuizCode(Long quizId) {
		// Tạo code 6 ký tự: 3 chữ cái + 3 số
		String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String numbers = "0123456789";
		
		StringBuilder code = new StringBuilder();
		Random random = new Random();
		
		// Tạo 3 chữ cái ngẫu nhiên
		for (int i = 0; i < 3; i++) {
			code.append(letters.charAt(random.nextInt(letters.length())));
		}
		
		// Tạo 3 số ngẫu nhiên
		for (int i = 0; i < 3; i++) {
			code.append(numbers.charAt(random.nextInt(numbers.length())));
		}
		
		String generatedCode = code.toString();
		
		// Kiểm tra xem code đã tồn tại chưa
		if (quizRepo.existsByQuizCode(generatedCode)) {
			// Nếu đã tồn tại, tạo lại
			return generateQuizCode(quizId);
		}
		
		return generatedCode;
	}

	// ✅ THÊM METHOD TÌM QUIZ THEO CODE
	public Optional<Quiz> findByQuizCode(String quizCode) {
		return quizRepo.findByQuizCode(quizCode.toUpperCase());
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

			// ✅ CẬP NHẬT IMAGE NẾU CÓ
			if (updatedQuiz.getImage() != null && !updatedQuiz.getImage().trim().isEmpty()) {
				// Tìm image record hiện tại
				Image existingImage = imageRepo.findByQuizId(id);

				if (existingImage != null) {
					// Cập nhật image hiện tại
					existingImage.setUrl(updatedQuiz.getImage());
					imageRepo.save(existingImage);
				} else {
					// Tạo image record mới
					Image newImage = new Image();
					newImage.setUrl(updatedQuiz.getImage());
					newImage.setQuiz(quiz);
					imageRepo.save(newImage);
				}
			}

			// ✅ CẬP NHẬT QUIZ.IMAGE FIELD
			quiz.setImage(updatedQuiz.getImage());

			return quizRepo.save(quiz);
		});
	}

	// Lấy danh sách quiz theo userId có phân trang
	public Page<Quiz> getQuizzesByUserPaginated(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return quizRepo.findByUserIdAndDeletedFalse(userId, pageable);
	}

	public Page<Quiz> getPublicQuizzes(Boolean isPublic, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return quizRepo.findByIsPublicAndDeletedFalse(isPublic, pageable);
	}

	// ✅ THÊM CÁC METHOD CHO SOFT DELETE
	// Lấy tất cả quiz của user (kể cả đã xóa) - cho admin
	public Page<Quiz> getAllQuizzesByUserPaginated(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return quizRepo.findByUserId(userId, pageable);
	}

	// Lấy quiz đã bị xóa mềm của user
	public Page<Quiz> getDeletedQuizzesByUserPaginated(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("deletedAt").descending());
		return quizRepo.findByUserIdAndDeletedTrue(userId, pageable);
	}

	// Soft delete quiz
	@Transactional
	public boolean softDeleteQuiz(Long id, Long deletedByUserId) {
		System.out.println("🔍 Checking if quiz exists: " + id);
		if (quizRepo.existsByIdAndDeletedFalse(id)) {
			System.out.println("✅ Quiz exists, proceeding with soft deletion");
			try {
				Optional<Quiz> quizOpt = quizRepo.findById(id);
				if (quizOpt.isPresent()) {
					Quiz quiz = quizOpt.get();
					quiz.setDeleted(true); // ✅ SỬA: Sử dụng Boolean.TRUE
					quiz.setDeletedAt(LocalDateTime.now());
					
					// Set user who deleted (nếu cần)
					if (deletedByUserId != null) {
						User deletedBy = loginService.findById(deletedByUserId);
						if (deletedBy != null) {
							quiz.setDeletedBy(deletedBy);
						}
					}
					
					quizRepo.save(quiz);
					System.out.println("✅ Quiz soft deleted successfully");
					return true;
				}
				return false;
			} catch (Exception e) {
				System.err.println("❌ Error soft deleting quiz: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("❌ Quiz not found or already deleted: " + id);
			return false;
		}
	}

	// Restore quiz (khôi phục quiz đã xóa mềm)
	@Transactional
	public boolean restoreQuiz(Long id) {
		System.out.println("🔍 Attempting to restore quiz: " + id);
		if (quizRepo.existsById(id)) {
			try {
				quizRepo.restoreQuiz(id);
				System.out.println("✅ Quiz restored successfully");
				return true;
			} catch (Exception e) {
				System.err.println("❌ Error restoring quiz: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("❌ Quiz not found: " + id);
			return false;
		}
	}

	// Hard delete quiz (xóa hoàn toàn)
	@Transactional
	public boolean hardDeleteQuiz(Long id) {
		System.out.println("🔍 Checking if quiz exists: " + id);
		if (quizRepo.existsById(id)) {
			System.out.println("✅ Quiz exists, proceeding with hard deletion");
			try {
				// 1. Xóa image trước
				Image image = imageRepo.findByQuizId(id);
				if (image != null) {
					System.out.println("🗑️ Deleting image: " + image.getUrl());
					imageRepo.delete(image);
				}
				
				// 2. Xóa results trước (vì results reference đến quiz)
				System.out.println("🗑️ Deleting results for quiz: " + id);
				resultService.deleteResultsByQuizId(id);
				
				// 3. Lấy questions của quiz
				List<Question> questions = questionService.getQuestionsByQuizId(id);
				System.out.println("🗑️ Found " + (questions != null ? questions.size() : 0) + " questions to delete");
				if (questions != null && !questions.isEmpty()) {
					// 4. Xóa answers trước (vì answers reference đến questions)
					for (Question question : questions) {
						System.out.println("🗑️ Deleting answers for question: " + question.getId());
						answerService.deleteByQuestionId(question.getId());
					}
					
					// 5. Xóa questions
					for (Question question : questions) {
						System.out.println("🗑️ Deleting question: " + question.getId());
						questionService.deleteQuestion(question.getId());
					}
				}
				
				// 6. Cuối cùng xóa quiz
				System.out.println("🗑️ Deleting quiz: " + id);
				quizRepo.deleteById(id);
				System.out.println("✅ Quiz hard deleted successfully");
				return true;
			} catch (Exception e) {
				System.err.println("❌ Error hard deleting quiz: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		} else {
			System.out.println("❌ Quiz not found: " + id);
			return false;
		}
	}

	// ✅ GIỮ LẠI METHOD CŨ ĐỂ TƯƠNG THÍCH NGƯỢC
	@Transactional
	public boolean deleteQuiz(Long id) {
		// Mặc định sử dụng soft delete
		return softDeleteQuiz(id, null);
	}

	public Image uploadImageForQuiz(Quiz quiz, MultipartFile file) throws IOException {
		// Tạo folder nếu chưa có
		String uploadDir = "uploads/images";
		Files.createDirectories(Paths.get(uploadDir));

		// Đặt tên file
		String originalFilename = file.getOriginalFilename();
		String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
		String filename = UUID.randomUUID().toString() + extension;

		// Lưu file
		Path filePath = Paths.get(uploadDir, filename);
		Files.write(filePath, file.getBytes());

		// Lưu thông tin vào DB
		Image image = new Image();
		image.setUrl(filename);
		image.setQuiz(quiz);

		return imageRepo.save(image);
	}

	// ✅ PHƯƠNG THỨC IMPORT TỪ EXCEL - ĐÃ SỬA HOÀN CHỈNH
	public Quiz createQuizFromImport(QuizImportDto quizData, String username, boolean isPublic) {
		// Validate dữ liệu
		excelImportService.validateQuizData(quizData);

		// Tìm user
		User user = loginService.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("Không tìm thấy user");
		}

		// ✅ SỬA: Tìm category qua Repository
		Category category = categoryRepo.findById(quizData.getCategoryId()).orElse(null);
		if (category == null) {
			throw new RuntimeException("Không tìm thấy category");
		}

		// Tạo quiz
		Quiz quiz = new Quiz();
		quiz.setTitle(quizData.getTitle());
		quiz.setUser(user);
		quiz.setCategory(category);
		quiz.setPublic(isPublic); // ✅ SỬ DỤNG ISPUBLIC TỪ PARAMETER
		quiz.setCreatedAt(LocalDateTime.now());

		Quiz savedQuiz = quizRepo.save(quiz);

		// ✅ TẠO CODE CHO QUIZ IMPORT
		String quizCode = generateQuizCode(savedQuiz.getId());
		savedQuiz.setQuizCode(quizCode);
		savedQuiz.setCodeCreatedAt(LocalDateTime.now());
		Quiz finalQuiz = quizRepo.save(savedQuiz);

		// Tạo questions và answers
		for (QuestionImportDto questionDto : quizData.getQuestions()) {
			Question question = new Question();
			question.setContent(questionDto.getContent());
			question.setPoint(questionDto.getPoint());
			question.setTimeLimit(questionDto.getTimeLimit()); // ✅ SỬ DỤNG TIMELIMIT TỪ EXCEL
			question.setQuiz(savedQuiz); // ✅ SỬA: setQuiz thay vì setQuizId

			Question savedQuestion = questionService.createQuestion(question); // ✅ SỬA: createQuestion thay vì
																				// saveQuestion

			// Tạo answers
			for (AnswerImportDto answerDto : questionDto.getAnswers()) {
				Answer answer = new Answer();
				answer.setContent(answerDto.getContent());
				answer.setCorrect(answerDto.isCorrect());
				answer.setQuestion(savedQuestion); // ✅ SỬA: setQuestion thay vì setQuestionId

				// ✅ SỬA: Tạo Answer thông qua createMultipleAnswers hoặc trực tiếp save
				answerService.createMultipleAnswers(List.of(answer));
			}
		}

		return finalQuiz;
	}

	// ✅ PHƯƠNG THỨC IMPORT QUIZ TỪ FILE EXCEL
	public Quiz importQuizFromExcel(MultipartFile file, String title, String description, Long categoryId,
			String username, boolean isPublic) {
		try {
			// Parse Excel file with all parameters
			QuizImportDto quizData = excelImportService.parseExcelFile(file, title, description, categoryId);

			// Create quiz with isPublic parameter
			return createQuizFromImport(quizData, username, isPublic);

		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi import file Excel: " + e.getMessage(), e);
		}
	}

	// ✅ PHƯƠNG THỨC LẤY CHI TIẾT QUIZ VỚI THỐNG KÊ
	public Optional<QuizDetailDTO> getQuizDetail(Long id) {
		return quizRepo.findById(id).map(quiz -> {
			// Lấy thông tin cơ bản
			QuizDetailDTO detail = new QuizDetailDTO();
			detail.setId(quiz.getId());
			detail.setTitle(quiz.getTitle());
			detail.setImage(quiz.getImage());
			detail.setPublic(quiz.isPublic());
			detail.setCreatedAt(quiz.getCreatedAt());

			// Thông tin người tạo
			if (quiz.getUser() != null) {
				System.out.println("🔍 Setting creator info:");
				System.out.println("  - User ID: " + quiz.getUser().getId());
				System.out.println("  - Username: " + quiz.getUser().getUsername());
				System.out.println("  - Full Name: " + quiz.getUser().getFullName());

				detail.setCreatorName(quiz.getUser().getFullName() != null ? quiz.getUser().getFullName()
						: quiz.getUser().getUsername());
				detail.setCreatorAvatar(quiz.getUser().getAvatarUrl());
				detail.setCreatorId(quiz.getUser().getId());

				System.out.println("  - Set creatorId: " + detail.getCreatorId());
			} else {
				System.out.println("❌ Quiz user is null!");
			}

			// Thông tin danh mục
			if (quiz.getCategory() != null) {
				detail.setCategoryName(quiz.getCategory().getName());
			}

			// Thông tin tags
			if (quiz.getQuizTags() != null && !quiz.getQuizTags().isEmpty()) {
				List<String> tagNames = quiz.getQuizTags().stream()
						.map(qt -> qt.getTag().getName())
						.toList();
				detail.setTags(tagNames);
			}

			// Thống kê câu hỏi
			int totalQuestions = quiz.getQuestions().size();
			int totalPoints = quiz.getQuestions().stream()
					.mapToInt(q -> q.getPoint())
					.sum();
			int totalTime = quiz.getQuestions().stream()
					.mapToInt(q -> q.getTimeLimit())
					.sum();

			detail.setTotalQuestions(totalQuestions);
			detail.setTotalPoints(totalPoints);
			detail.setTotalTime(totalTime);

			// TODO: Thêm thống kê từ QuizAttempt khi có dữ liệu
			detail.setTotalPlays(0);
			detail.setAverageScore(0.0);
			detail.setUniqueParticipants(0);
			detail.setCompletionRate(0.0);
			detail.setAverageTime(0);

			return detail;
		});
	}
}