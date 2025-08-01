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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
				// ✅ LẤY THÔNG TIN USER ĐẦY ĐỦ TỪ DATABASE
				User fullUser = result.user();
				String token = jwtUtil.generateToken(fullUser.getUsername());

				Map<String, Object> response = new java.util.HashMap<>();
				response.put("status", "SUCCESS");
				response.put("token", token);
				response.put("message", "Đăng nhập thành công");
				response.put("user", Map.of(
						"id", fullUser.getId(),
						"username", fullUser.getUsername(),
						"email", fullUser.getEmail(),
						"fullName", fullUser.getFullName(),
						"role", fullUser.getRole()));
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

	// Phần lấy thông tin người dùng đã đăng nhập (DEPRECATED - sử dụng
	// /user/profile thay thế)
	@GetMapping("/profile")
	public ResponseEntity<?> getProfile(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bạn chưa đăng nhập");
		}

		String username = authentication.getName();
		User user = loginService.findByUsername(username);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
		}

		// Trả về thông tin user thay vì chỉ token
		Map<String, Object> response = new HashMap<>();
		response.put("id", user.getId());
		response.put("username", user.getUsername());
		response.put("email", user.getEmail());
		response.put("fullName", user.getFullName());
		response.put("role", user.getRole());

		return ResponseEntity.ok(response);
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

	@GetMapping("/user/profile")
	public ResponseEntity<?> getUserProfile(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ hoặc đã hết hạn");
		}

		try {
			String username = authentication.getName();
			User user = loginService.findByUsername(username);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
			}

			// Trả về thông tin profile
			Map<String, Object> response = new HashMap<>();
			response.put("id", user.getId());
			response.put("username", user.getUsername());
			response.put("email", user.getEmail());
			response.put("fullName", user.getFullName());
			response.put("bio", user.getBio());
			response.put("avatarUrl", user.getAvatarUrl());
			response.put("role", user.getRole());
			response.put("createdAt", user.getCreatedAt());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Lỗi khi lấy thông tin profile: " + e.getMessage());
		}
	}

	@PutMapping("/user/profile")
	public ResponseEntity<?> updateProfile(
			@RequestParam(required = false) String fullName,
			@RequestParam(required = false) String bio,
			@RequestParam(required = false) String avatarUrl,
			@RequestParam(required = false) MultipartFile avatar,
			Authentication authentication) {

		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ hoặc đã hết hạn");
		}

		try {
			String username = authentication.getName();
			User user = loginService.findByUsername(username);

			if (user == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy người dùng");
			}

			// Cập nhật thông tin cơ bản
			if (fullName != null && !fullName.trim().isEmpty()) {
				user.setFullName(fullName.trim());
			}

			if (bio != null) {
				user.setBio(bio.trim());
			}

			// Xử lý avatar
			if (avatar != null && !avatar.isEmpty()) {
				// Upload file avatar
				String uploadDir = "uploads/avatars";
				Files.createDirectories(Paths.get(uploadDir));

				String originalFilename = avatar.getOriginalFilename();
				String extension = "";
				if (originalFilename != null && originalFilename.contains(".")) {
					extension = originalFilename.substring(originalFilename.lastIndexOf("."));
				}
				String filename = UUID.randomUUID().toString() + extension;

				Path filePath = Paths.get(uploadDir, filename);
				Files.write(filePath, avatar.getBytes());

				user.setAvatarUrl("/api/upload/avatars/" + filename);
			} else if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
				// Sử dụng URL avatar
				user.setAvatarUrl(avatarUrl.trim());
			}

			// Lưu vào database
			loginService.save(user);

			// Trả về thông tin đã cập nhật
			Map<String, Object> response = new HashMap<>();
			response.put("id", user.getId());
			response.put("username", user.getUsername());
			response.put("email", user.getEmail());
			response.put("fullName", user.getFullName());
			response.put("bio", user.getBio());
			response.put("avatarUrl", user.getAvatarUrl());
			response.put("role", user.getRole());

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Lỗi khi cập nhật profile: " + e.getMessage());
		}
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

	// ✅ ENDPOINT TEST ĐỂ KIỂM TRA DATABASE
	@GetMapping("/test/users")
	public ResponseEntity<?> testUsers() {
		try {
			// Lấy tất cả users để test
			java.util.List<User> users = ((java.util.List<User>) userRepo.findAll());

			Map<String, Object> response = new HashMap<>();
			response.put("totalUsers", users.size());
			response.put("users", users.stream().map(user -> Map.of(
					"id", user.getId(),
					"username", user.getUsername(),
					"email", user.getEmail(),
					"role", user.getRole() != null ? user.getRole() : "NULL")).toList());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error: " + e.getMessage());
		}
	}

}
