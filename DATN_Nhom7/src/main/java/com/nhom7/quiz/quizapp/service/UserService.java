package com.nhom7.quiz.quizapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.UserRepo;

@Service
public class UserService {
	// Viết các phương thức để xử lý người dùng
	@Autowired
	private UserRepo userRepo;

	// Trả về tất cả người dùng
	public List<User> getAllUser() {
		return userRepo.findAll();
	}

	// Kiểm tra xem người dùng đã tồn tại hay chưa
	public boolean existsByUsername(String username) {
		return userRepo.findByUsername(username).isPresent();
	}

	// Kiểm tra xem email đã tồn tại hay chưa
	public boolean existsByEmail(String email) {
		return userRepo.findByUsername(email).isPresent();
	}

	// Lưu người dùng mới
	public User saveUser(User user) {
		return userRepo.save(user);
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	// Kiểm tra đăng nhập
	public Optional<User> checkLogin(String username, String password) {
		return userRepo.findByUsername(username)
				.filter(user -> passwordEncoder.matches(user.getPassword(), password));
	}

}
