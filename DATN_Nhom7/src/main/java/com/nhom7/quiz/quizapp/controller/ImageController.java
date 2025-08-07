package com.nhom7.quiz.quizapp.controller;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nhom7.quiz.quizapp.model.Image;
import com.nhom7.quiz.quizapp.service.ImageService;

@RestController
@RequestMapping("/api")
public class ImageController {

    private final String uploadDir = "uploads/images";

    @Autowired
    private ImageService imageService;

    
    @GetMapping("/image/quiz/{quizId}")
    public ResponseEntity<Resource> getImageByQuizId(@PathVariable Long quizId) throws IOException {
        System.out.println("🖼️ Image request for quiz ID: " + quizId);
        
        Image image = imageService.getImageByQuizId(quizId);
        
        // ✅ DEBUG: Kiểm tra image từ database
        System.out.println("🔍 Image from database: " + image);
        if (image != null) {
            System.out.println("🔍 Image URL: " + image.getUrl());
            System.out.println("🔍 Image Quiz ID: " + (image.getQuiz() != null ? image.getQuiz().getId() : "NULL"));
        }

        if (image == null || image.getUrl() == null) {
            System.out.println("❌ No image found for quiz ID: " + quizId);
            return ResponseEntity.notFound().build();
        }

        String imageUrl = image.getUrl();
        String filename;
        
        // ✅ FIX: Handle different URL formats properly
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            // External URL - extract filename
            filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        } else if (imageUrl.startsWith("/uploads/")) {
            // Internal path - extract filename only
            filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        } else {
            // Just filename
            filename = imageUrl;
        }
        
        Path filePath = Paths.get(uploadDir, filename);

        if (!Files.exists(filePath)) {
            System.out.println("❌ Image file not found: " + filePath);
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));
        System.out.println("✅ Image found: " + image);
        System.out.println("✅ Image URL: " + imageUrl);
        System.out.println("✅ Extracted filename: " + filename);
        System.out.println("✅ File path: " + filePath);
        System.out.println("✅ File exists: " + Files.exists(filePath));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null
                        ? contentType
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    
    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "File không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Chỉ chấp nhận file hình ảnh");
                return ResponseEntity.badRequest().body(error);
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Kích thước file không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(error);
            }

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/api/upload/images/" + newFilename;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upload thành công");
            response.put("imageUrl", imageUrl);
            response.put("filename", newFilename);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi lưu file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/upload/images/{filename}")
    public ResponseEntity<Resource> getUploadedImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ ENDPOINT MỚI: XỬ LÝ URL THÀNH FILENAME
    @PostMapping("/upload/url")
    public ResponseEntity<?> uploadImageFromUrl(@RequestParam("imageUrl") String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "URL không được để trống"));
            }

            // Download image from URL
            URL url = new URL(imageUrl);
            String originalFilename = Paths.get(url.getPath()).getFileName().toString();
            
            // Generate new filename
            String fileExtension = "";
            if (originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                fileExtension = ".jpg"; // Default extension
            }
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Create upload directory
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Download and save file
            Path filePath = uploadPath.resolve(newFilename);
            try (var inputStream = url.openStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Download thành công",
                "filename", newFilename
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Lỗi khi download: " + e.getMessage()));
        }
    }

    
    @GetMapping("/upload/avatars/{filename}")
    public ResponseEntity<Resource> getAvatarImage(@PathVariable String filename) {
        try {
            String avatarDir = "uploads/avatars";
            Path filePath = Paths.get(avatarDir).resolve(filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}