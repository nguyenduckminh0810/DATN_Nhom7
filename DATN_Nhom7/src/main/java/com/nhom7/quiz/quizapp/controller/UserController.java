package com.nhom7.quiz.quizapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.service.userService.ReginService;
import com.nhom7.quiz.quizapp.service.userService.LoginService.LoginResult;
import com.nhom7.quiz.quizapp.service.userService.ReginService.RegisterResult;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class UserController {

	// Form đăng ký người dùng mới
	@Autowired
	private ReginService reginService;

	@PostMapping("/register")
	public ResponseEntity<?> registerUser(@RequestBody User user) {
		RegisterResult result = reginService.register(user);

		return switch (result.status()) {
			case EMAIL_EXISTS -> ResponseEntity.badRequest().body("Email đã tồn tại");
			case USERNAME_EXISTS -> ResponseEntity.badRequest().body("Username đã tồn tại");
			case FULL_NAME_REQUIRED -> ResponseEntity.badRequest().body("Chưa điển tên đầy đủ");
			case SUCCESS -> ResponseEntity.ok("Đăng ký thành công");
		};
	}

	// Phần đăng nhập người dùng
	@Autowired
	private LoginService loginService;

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		LoginResult result = loginService.checkLogin(user.getUsername(), user.getPassword());

		return switch (result.status()) {
			case SUCCESS -> ResponseEntity.ok(Map.of(
					"status", "SUCCESS",
					"message", "Đăng nhập thành công"));
			case USER_NOT_FOUND, WRONG_PASSWORD -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
					"status", result.status().toString(),
					"message", "Tên đăng nhập hoặc mật khẩu không đúng"));
		};
	}
}
