package com.nhom7.quiz.quizapp.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(name = "full_name", length = 100)
	private String fullName;

	@Column(name = "avatar_url", length = 255)
	private String avatarUrl;

	@Column(columnDefinition = "TEXT")
	private String bio;

	@Column(length = 20)
	private String role = "USER";

	@Column(name = "is_banned", nullable = false, columnDefinition = "bit default 0")
	private boolean isBanned = false;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();
	// Sau này dùng
	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<Notification> notifications = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<QuizReview> reviews = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<Report> reports = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<QuizAttempt> attempts = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<Quiz> quizzes = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<Result> results = new HashSet<>();

	@JsonIgnore
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private Set<Comment> comments = new HashSet<>();

	public User() {
		super();
	}

	public User(Long id, String username, String email, String password, String fullName, String avatarUrl, String bio,
			String role, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.fullName = fullName;
		this.avatarUrl = avatarUrl;
		this.bio = bio;
		this.role = role;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getBio() {
		return bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isBanned() {
		return isBanned;
	}

	public void setBanned(boolean isBanned) {
		this.isBanned = isBanned;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
