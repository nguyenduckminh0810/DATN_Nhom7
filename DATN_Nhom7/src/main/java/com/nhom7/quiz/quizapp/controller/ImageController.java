package com.nhom7.quiz.quizapp.controller;

import java.io.IOException;
import java.nio.file.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.nhom7.quiz.quizapp.model.Image;
import com.nhom7.quiz.quizapp.service.ImageService;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private final String uploadDir = "uploads/images"; // ✅ CHỈNH LẠI CHO KHỚP

    @Autowired
    private ImageService imageService;

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<Resource> getImageByQuizId(@PathVariable Long quizId) throws IOException {
        Image image = imageService.getImageByQuizId(quizId);

        if (image == null || image.getUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = image.getUrl();
        Path filePath = Paths.get(uploadDir, filename);

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(filePath));
        System.out.println("Image: " + image);
        System.out.println("Image URL: " + image.getUrl());
        System.out.println("File path: " + filePath);
        System.out.println("File exists: " + Files.exists(filePath));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null
                        ? contentType
                        : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
