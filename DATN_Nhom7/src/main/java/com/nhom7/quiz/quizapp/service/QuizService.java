package com.nhom7.quiz.quizapp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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
import com.nhom7.quiz.quizapp.repository.CategoryRepo;  // ✅ THÊM IMPORT
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
	private CategoryRepo categoryRepo;  // ✅ SỬA: Dùng Repository thay vì Service

	@Autowired
	private ExcelImportService excelImportService;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private AnswerService answerService;

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
		return quizRepo.findByUserId(userId, pageable);
	}

	public Page<Quiz> getPublicQuizzes(boolean isPublic, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return quizRepo.findByIsPublic(isPublic, pageable);
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

	// Xoá quiz
	public boolean deleteQuiz(Long id) {
		if (quizRepo.existsById(id)) {
			Image image = imageRepo.findByQuizId(id);
			if (image != null) {
				imageRepo.delete(image); // Xoá tay từng ảnh
			}
			quizRepo.deleteById(id);
			return true;
		}
		return false;
	}

	// ✅ PHƯƠNG THỨC IMPORT TỪ EXCEL - ĐÃ SỬA HOÀN CHỈNH
	public Quiz createQuizFromImport(QuizImportDto quizData, String username) {
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
		quiz.setPublic(false); // Mặc định là private
		quiz.setCreatedAt(LocalDateTime.now());
		
		Quiz savedQuiz = quizRepo.save(quiz);
		
		// Tạo questions và answers
		for (QuestionImportDto questionDto : quizData.getQuestions()) {
			Question question = new Question();
			question.setContent(questionDto.getContent());
			question.setPoint(questionDto.getPoint());
			question.setQuiz(savedQuiz);  // ✅ SỬA: setQuiz thay vì setQuizId
			
			Question savedQuestion = questionService.createQuestion(question);  // ✅ SỬA: createQuestion thay vì saveQuestion
			
			// Tạo answers
			for (AnswerImportDto answerDto : questionDto.getAnswers()) {
				Answer answer = new Answer();
				answer.setContent(answerDto.getContent());
				answer.setCorrect(answerDto.isCorrect());
				answer.setQuestion(savedQuestion);  // ✅ SỬA: setQuestion thay vì setQuestionId
				
				// ✅ SỬA: Tạo Answer thông qua createMultipleAnswers hoặc trực tiếp save
				answerService.createMultipleAnswers(List.of(answer));
			}
		}
		
		return savedQuiz;
	}

	// ✅ PHƯƠNG THỨC IMPORT QUIZ TỪ FILE EXCEL
	public Quiz importQuizFromExcel(MultipartFile file, String title, String description, Long categoryId, String username) {
		try {
			// Parse Excel file with all parameters
			QuizImportDto quizData = excelImportService.parseExcelFile(file, title, description, categoryId);
			
			// Create quiz
			return createQuizFromImport(quizData, username);
			
		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi import file Excel: " + e.getMessage(), e);
		}
	}
}