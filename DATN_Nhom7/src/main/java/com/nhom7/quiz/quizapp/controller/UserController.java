package com.nhom7.quiz.quizapp.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.config.JwtUtil;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.service.userService.ReginService;
import com.nhom7.quiz.quizapp.service.userService.LoginService.LoginResult;
import com.nhom7.quiz.quizapp.service.userService.ReginService.RegisterResult;

import org.springframework.web.bind.annotation.GetMapping;
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
			case EMAIL_EXISTS -> {
				Map<String, String> response = new java.util.HashMap<>();
				response.put("status", "EMAIL_EXISTS");
				response.put("message", "Email đã tồn tại");
				yield ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			case USERNAME_EXISTS -> {
				Map<String, String> response = new java.util.HashMap<>();
				response.put("status", "USERNAME_EXISTS");
				response.put("message", "Username đã tồn tại");
				yield ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			case FULL_NAME_REQUIRED -> {
				Map<String, String> response = new java.util.HashMap<>();
				response.put("status", "FULL_NAME_REQUIRED");
				response.put("message", "Chưa điền tên đầy đủ");
				yield ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
			}
			case SUCCESS -> {
				Map<String, String> response = new java.util.HashMap<>();
				response.put("status", "SUCCESS");
				response.put("message", "Đăng ký thành công");// Bạn có thể generate token thực nếu muốn
				yield ResponseEntity.ok(response);
			}
		};
	}

	// Token JWT
	@Autowired
	private JwtUtil jwtUtil;

	// Phần đăng nhập người dùng
	@Autowired
	private LoginService loginService;

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		LoginResult result = loginService.checkLogin(user.getUsername(), user.getPassword());

		return switch (result.status()) {
			case SUCCESS -> {
				String token = jwtUtil.generateToken(user.getUsername());
				Map<String, String> response = new java.util.HashMap<>();
				response.put("status", "SUCCESS");
				response.put("token", token);
				response.put("message", "Đăng nhập thành công");
				yield ResponseEntity.ok(response);
			}
			case USER_NOT_FOUND, WRONG_PASSWORD -> {
				Map<String, String> response = new java.util.HashMap<>();
				response.put("status", result.status().toString());
				response.put("message", "Tên đăng nhập hoặc mật khẩu không đúng");
				yield ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
			}
		};
	}

	// Phần lấy thông tin người dùng đã đăng nhập
	@GetMapping("/profile")
	public ResponseEntity<?> getProfile(@RequestBody User user) {
		String token = jwtUtil.generateToken(user.getUsername());
		String username = jwtUtil.extractUsername(token); // Lấy tên người dùng từ token
		if (username == null || username.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng nhập");
		}
		return ResponseEntity.ok(token);
	}

	@GetMapping("/user")
	public ResponseEntity<?> getUserIdByToken(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ hoặc đã hết hạn");
		}

		String username = authentication.getName(); // Lấy từ SecurityContext
		User user = loginService.findByUsername(username);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
		}

		return ResponseEntity.ok(Map.of("user_id", user.getId()));
	}

}
