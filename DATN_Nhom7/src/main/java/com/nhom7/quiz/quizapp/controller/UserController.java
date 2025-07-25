package com.nhom7.quiz.quizapp.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nhom7.quiz.quizapp.config.JwtUtil;
import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.UserRepo;
import com.nhom7.quiz.quizapp.service.userService.LoginService;
import com.nhom7.quiz.quizapp.service.userService.ReginService;
import com.nhom7.quiz.quizapp.service.userService.LoginService.LoginResult;
import com.nhom7.quiz.quizapp.service.userService.ReginService.RegisterResult;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

	// Lấy user theo id
	@GetMapping("/user/{id}")
	public ResponseEntity<?> getUserById(@PathVariable Long id) {
		User user = loginService.findById(id);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
		}

		Map<String, Object> result = new HashMap<>();
		result.put("id", user.getId());
		result.put("username", user.getUsername());
		result.put("fullName", user.getFullName());
		result.put("email", user.getEmail());
		result.put("bio", user.getBio());
		result.put("avatarUrl", user.getAvatarUrl());
		result.put("createAt", user.getCreatedAt());

		return ResponseEntity.ok(result);
	}

	@Autowired
	private UserRepo userRepo;

	// Cập nhật thông tin người dùng
	@PutMapping("/user/{id}")
	public ResponseEntity<?> updateUserProfile(
			@PathVariable Long id,
			@RequestParam("fullName") String fullName,
			@RequestParam("username") String username,
			@RequestParam("bio") String bio,
			@RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {

		Optional<User> userOpt = userRepo.findById(id);
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Người dùng không tồn tại.");
		}

		User user = userOpt.get();
		user.setFullName(fullName);
		user.setUsername(username);
		user.setBio(bio);

		// Nếu có avatar mới thì xử lý lưu ảnh
		if (avatarFile != null && !avatarFile.isEmpty()) {
			try {
				// Dùng ../ để đi từ DATN-Nhom7 sang DATN/uploads/images
				Path uploadPath = Paths.get("../uploads/images").toAbsolutePath().normalize();
				Files.createDirectories(uploadPath); // đảm bảo thư mục tồn tại

				String fileName = UUID.randomUUID() + "_" + avatarFile.getOriginalFilename();
				Path filePath = uploadPath.resolve(fileName);

				// Ghi file vào đúng nơi
				avatarFile.transferTo(filePath.toFile());

				// Đường dẫn lưu trong DB hoặc trả về client
				String imageUrl = "/uploads/images/" + fileName;
				user.setAvatarUrl(imageUrl);

			} catch (IOException e) {
				e.printStackTrace();
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể lưu ảnh đại diện.");
			}
		}

		userRepo.save(user);

		Map<String, Object> result = new HashMap<>();
		result.put("fullName", user.getFullName());
		result.put("username", user.getUsername());
		result.put("bio", user.getBio());
		result.put("avatarUrl", user.getAvatarUrl());

		return ResponseEntity.ok(result);
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	// Đổi mật khẩu
	@PutMapping("/user/{id}/change-password")
	public ResponseEntity<?> changePassword(
			@PathVariable Long id,
			@RequestBody Map<String, String> passwordMap) {

		Optional<User> userOpt = userRepo.findById(id);
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng.");
		}

		User user = userOpt.get();

		String currentPassword = passwordMap.get("currentPassword");
		String newPassword = passwordMap.get("newPassword");

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu hiện tại không đúng.");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepo.save(user);

		return ResponseEntity.ok("Đổi mật khẩu thành công.");
	}

	// Xóa tài khoản
	@DeleteMapping("/user/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {
		Optional<User> userOpt = userRepo.findById(id);
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng.");
		}

		userRepo.deleteById(id);
		return ResponseEntity.ok("Tài khoản đã được xoá.");
	}

}
