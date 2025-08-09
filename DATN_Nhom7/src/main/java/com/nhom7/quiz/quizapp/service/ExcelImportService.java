package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExcelImportService {

    // Kiểm tra quyền admin (đã mở public: luôn cho phép)
    private void checkAdminPermission() {
        // No-op: mở quyền import public cho mọi người dùng đã đăng nhập
    }

    // ✅ THÊM METHOD PREVIEW
    public QuizImportDto previewExcelFile(MultipartFile file, String title, String description, Long categoryId)
            throws IOException {
        checkAdminPermission();
        return parseExcelFile(file, title, description, categoryId);
    }

    public QuizImportDto parseExcelFile(MultipartFile file, String title, String description, Long categoryId)
            throws IOException {
        checkAdminPermission();
        
        List<QuestionImportDto> questions = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // ✅ VALIDATE FILE KHÔNG RỖNG
            if (sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("File Excel không có dữ liệu. Vui lòng thêm ít nhất 1 câu hỏi.");
            }

            // Bỏ qua header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                try {
                    QuestionImportDto question = parseQuestionFromRow(row);
                    if (question != null && !question.getContent().trim().isEmpty()) {
                        questions.add(question);
                    }
                } catch (IllegalArgumentException e) {
                    // Re-throw với thông tin chi tiết hơn
                    throw new IllegalArgumentException("Lỗi ở dòng " + (i + 1) + ": " + e.getMessage());
                }
            }

            // ✅ VALIDATE CÓ ÍT NHẤT 1 CÂU HỎI
            if (questions.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy câu hỏi hợp lệ nào trong file Excel. Vui lòng kiểm tra lại dữ liệu.");
            }
        } catch (IOException e) {
            throw new IOException("Không thể đọc file Excel. Vui lòng kiểm tra định dạng file.");
        }

        // ✅ KHÔNG ĐỌC ẢNH TỪ EXCEL NỮA - SẼ UPLOAD TRỰC TIẾP TỪ CLIENT
        return new QuizImportDto(title, description, categoryId, null, questions);
    }

    private QuestionImportDto parseQuestionFromRow(Row row) {
        try {
            // ✅ CẤU TRÚC EXCEL: Câu hỏi | Đáp án A | Đáp án B | Đáp án C | Đáp án D | Đáp án đúng | Thời gian
            String questionContent = getCellStringValue(row.getCell(0)); // Câu hỏi
            if (questionContent == null || questionContent.trim().isEmpty()) {
                return null;
            }

            String answerA = getCellStringValue(row.getCell(1)); // Đáp án A
            String answerB = getCellStringValue(row.getCell(2)); // Đáp án B
            String answerC = getCellStringValue(row.getCell(3)); // Đáp án C
            String answerD = getCellStringValue(row.getCell(4)); // Đáp án D
            String correctAnswer = getCellStringValue(row.getCell(5)); // Đáp án đúng

            // ✅ VALIDATE ĐÁP ÁN
            if (answerA == null || answerA.trim().isEmpty()) {
                throw new IllegalArgumentException("Đáp án A không được để trống ở dòng " + (row.getRowNum() + 1));
            }
            if (answerB == null || answerB.trim().isEmpty()) {
                throw new IllegalArgumentException("Đáp án B không được để trống ở dòng " + (row.getRowNum() + 1));
            }
            if (answerC == null || answerC.trim().isEmpty()) {
                throw new IllegalArgumentException("Đáp án C không được để trống ở dòng " + (row.getRowNum() + 1));
            }
            if (answerD == null || answerD.trim().isEmpty()) {
                throw new IllegalArgumentException("Đáp án D không được để trống ở dòng " + (row.getRowNum() + 1));
            }

            // ✅ VALIDATE ĐÁP ÁN ĐÚNG
            if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
                throw new IllegalArgumentException("Đáp án đúng không được để trống ở dòng " + (row.getRowNum() + 1));
            }
            if (!correctAnswer.equalsIgnoreCase("A") && !correctAnswer.equalsIgnoreCase("B") && 
                !correctAnswer.equalsIgnoreCase("C") && !correctAnswer.equalsIgnoreCase("D")) {
                throw new IllegalArgumentException("Đáp án đúng phải là A, B, C hoặc D ở dòng " + (row.getRowNum() + 1));
            }

            // ✅ VALIDATE THỜI GIAN
            int timeLimit = 30; // Mặc định 30 giây
            Cell timeCell = row.getCell(6);
            if (timeCell != null && timeCell.getCellType() == CellType.NUMERIC) {
                timeLimit = (int) timeCell.getNumericCellValue();
                
                // Kiểm tra range thời gian (5-300 giây)
                if (timeLimit < 5) {
                    throw new IllegalArgumentException("Thời gian phải từ 5 giây trở lên ở dòng " + (row.getRowNum() + 1) + " (hiện tại: " + timeLimit + "s)");
                }
                if (timeLimit > 300) {
                    throw new IllegalArgumentException("Thời gian phải từ 300 giây trở xuống ở dòng " + (row.getRowNum() + 1) + " (hiện tại: " + timeLimit + "s)");
                }
            }

            // Tạo danh sách answers
            List<AnswerImportDto> answers = Arrays.asList(
                    new AnswerImportDto(answerA, "A".equalsIgnoreCase(correctAnswer)),
                    new AnswerImportDto(answerB, "B".equalsIgnoreCase(correctAnswer)),
                    new AnswerImportDto(answerC, "C".equalsIgnoreCase(correctAnswer)),
                    new AnswerImportDto(answerD, "D".equalsIgnoreCase(correctAnswer)));

            return new QuestionImportDto(questionContent, 1, timeLimit, answers); // ✅ SỬ DỤNG TIMELIMIT

        } catch (Exception e) {
            System.err.println("Lỗi parse row " + row.getRowNum() + ": " + e.getMessage());
            throw e; // Re-throw để hiển thị lỗi cho user
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    public void validateQuizData(QuizImportDto quizData) throws IllegalArgumentException {
        checkAdminPermission();
        
        if (quizData.getTitle() == null || quizData.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên quiz không được để trống");
        }

        if (quizData.getQuestions() == null || quizData.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("Quiz phải có ít nhất 1 câu hỏi");
        }

        for (int i = 0; i < quizData.getQuestions().size(); i++) {
            QuestionImportDto question = quizData.getQuestions().get(i);

            if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("Câu hỏi " + (i + 1) + " không được để trống");
            }

            if (question.getAnswers() == null || question.getAnswers().size() != 4) {
                throw new IllegalArgumentException("Câu hỏi " + (i + 1) + " phải có đúng 4 đáp án");
            }

            // ✅ VALIDATE ĐÁP ÁN KHÔNG RỖNG
            for (int j = 0; j < question.getAnswers().size(); j++) {
                AnswerImportDto answer = question.getAnswers().get(j);
                if (answer.getContent() == null || answer.getContent().trim().isEmpty()) {
                    throw new IllegalArgumentException("Đáp án " + (char)('A' + j) + " của câu hỏi " + (i + 1) + " không được để trống");
                }
            }

            boolean hasCorrectAnswer = question.getAnswers().stream().anyMatch(AnswerImportDto::isCorrect);
            if (!hasCorrectAnswer) {
                throw new IllegalArgumentException("Câu hỏi " + (i + 1) + " phải có ít nhất 1 đáp án đúng");
            }

            // ✅ VALIDATE THỜI GIAN
            if (question.getTimeLimit() < 5) {
                throw new IllegalArgumentException("Thời gian của câu hỏi " + (i + 1) + " phải từ 5 giây trở lên (hiện tại: " + question.getTimeLimit() + "s)");
            }
            if (question.getTimeLimit() > 300) {
                throw new IllegalArgumentException("Thời gian của câu hỏi " + (i + 1) + " phải từ 300 giây trở xuống (hiện tại: " + question.getTimeLimit() + "s)");
            }
        }
    }
}