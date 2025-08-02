package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ExcelImportService {

    // ✅ THÊM METHOD PREVIEW
    public QuizImportDto previewExcelFile(MultipartFile file, String title, String description, Long categoryId)
            throws IOException {
        return parseExcelFile(file, title, description, categoryId);
    }

    public QuizImportDto parseExcelFile(MultipartFile file, String title, String description, Long categoryId)
            throws IOException {
        List<QuestionImportDto> questions = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Bỏ qua header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                QuestionImportDto question = parseQuestionFromRow(row);
                if (question != null && !question.getContent().trim().isEmpty()) {
                    questions.add(question);
                }
            }
        }

        // ✅ KHÔNG ĐỌC ẢNH TỪ EXCEL NỮA - SẼ UPLOAD TRỰC TIẾP TỪ CLIENT
        return new QuizImportDto(title, description, categoryId, null, questions);
    }

    private QuestionImportDto parseQuestionFromRow(Row row) {
        try {
            // ✅ CẤU TRÚC EXCEL: Câu hỏi | Đáp án A | Đáp án B | Đáp án C | Đáp án D | Đáp
            // án đúng | Giải thích | Thời gian
            String questionContent = getCellStringValue(row.getCell(0)); // Câu hỏi
            if (questionContent == null || questionContent.trim().isEmpty()) {
                return null;
            }

            String answerA = getCellStringValue(row.getCell(1)); // Đáp án A
            String answerB = getCellStringValue(row.getCell(2)); // Đáp án B
            String answerC = getCellStringValue(row.getCell(3)); // Đáp án C
            String answerD = getCellStringValue(row.getCell(4)); // Đáp án D
            String correctAnswer = getCellStringValue(row.getCell(5)); // Đáp án đúng
            String explanation = getCellStringValue(row.getCell(6)); // Giải thích (bỏ qua)

            // Lấy thời gian (cột 7) - mặc định 30 giây nếu không có
            int timeLimit = 30;
            Cell timeCell = row.getCell(7);
            if (timeCell != null && timeCell.getCellType() == CellType.NUMERIC) {
                timeLimit = (int) timeCell.getNumericCellValue();
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
            return null;
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

            boolean hasCorrectAnswer = question.getAnswers().stream().anyMatch(AnswerImportDto::isCorrect);
            if (!hasCorrectAnswer) {
                throw new IllegalArgumentException("Câu hỏi " + (i + 1) + " phải có ít nhất 1 đáp án đúng");
            }
        }
    }
}