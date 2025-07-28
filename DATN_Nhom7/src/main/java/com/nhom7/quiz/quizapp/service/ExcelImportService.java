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
    
    public QuizImportDto parseExcelFile(MultipartFile file, String title, String description, Long categoryId) throws IOException {
        List<QuestionImportDto> questions = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Bỏ qua header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                QuestionImportDto question = parseQuestionFromRow(row);
                if (question != null && !question.getContent().trim().isEmpty()) {
                    questions.add(question);
                }
            }
        }
        
        return new QuizImportDto(title, description, categoryId, questions);
    }
    
    private QuestionImportDto parseQuestionFromRow(Row row) {
        try {
            // Cấu trúc Excel: STT | Câu hỏi | Đáp án A | Đáp án B | Đáp án C | Đáp án D | Đáp án đúng | Điểm
            String questionContent = getCellStringValue(row.getCell(1));
            if (questionContent == null || questionContent.trim().isEmpty()) {
                return null;
            }
            
            String answerA = getCellStringValue(row.getCell(2));
            String answerB = getCellStringValue(row.getCell(3));
            String answerC = getCellStringValue(row.getCell(4));
            String answerD = getCellStringValue(row.getCell(5));
            String correctAnswer = getCellStringValue(row.getCell(6)); // A, B, C, hoặc D
            
            // Lấy điểm (mặc định là 1 nếu không có)
            int point = 1;
            Cell pointCell = row.getCell(7);
            if (pointCell != null && pointCell.getCellType() == CellType.NUMERIC) {
                point = (int) pointCell.getNumericCellValue();
            }
            
            // Tạo danh sách answers
            List<AnswerImportDto> answers = Arrays.asList(
                new AnswerImportDto(answerA, "A".equalsIgnoreCase(correctAnswer)),
                new AnswerImportDto(answerB, "B".equalsIgnoreCase(correctAnswer)),
                new AnswerImportDto(answerC, "C".equalsIgnoreCase(correctAnswer)),
                new AnswerImportDto(answerD, "D".equalsIgnoreCase(correctAnswer))
            );
            
            return new QuestionImportDto(questionContent, point, answers);
            
        } catch (Exception e) {
            System.err.println("Lỗi parse row " + row.getRowNum() + ": " + e.getMessage());
            return null;
        }
    }
    
    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        
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