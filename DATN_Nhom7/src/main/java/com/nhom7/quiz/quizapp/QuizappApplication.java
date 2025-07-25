package com.nhom7.quiz.quizapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.nhom7.quiz.quizapp.model",
		"com.nhom7.quiz.quizapp.repository",
		"com.nhom7.quiz.quizapp.controller",
		"com.nhom7.quiz.quizapp.service",
		"com.nhom7.quiz.quizapp.config"
})
public class QuizappApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizappApplication.class, args);
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String rawPassword = "matkhau10";
		String encodedPassword = encoder.encode(rawPassword);
		System.out.println(encodedPassword);
	}

}
