package com.nhom7.quiz.quizapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reports")
public class Report {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "quiz_id")
	private Quiz quiz;

	@ManyToOne
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String reason;

	@Column(length = 20, nullable = false)
	private String status; // PENDING, RESOLVED, REJECTED

	@ManyToOne(optional = true)
	@JoinColumn(name = "reported_user_id", nullable = true)

	private User reportedUser;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	// THÊM FIELDS MỚI CHO ADMIN ACTION
	@Column(name = "admin_response", columnDefinition = "TEXT")
	private String adminResponse;

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	@ManyToOne
	@JoinColumn(name = "resolved_by")
	private User resolvedBy;

	public Report(Long id, User user, Quiz quiz, Comment comment, String reason, String status,
			User reportedUser, LocalDateTime createdAt, String adminResponse, LocalDateTime resolvedAt,
			User resolvedBy) {
		super();
		this.id = id;
		this.user = user;
		this.quiz = quiz;
		this.comment = comment;
		this.reason = reason;
		this.status = status;
		this.reportedUser = reportedUser;
		this.createdAt = createdAt;
		this.adminResponse = adminResponse;
		this.resolvedAt = resolvedAt;
		this.resolvedBy = resolvedBy;
	}

	public Report() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Quiz getQuiz() {
		return quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getReportedUser() {
		return reportedUser;
	}

	public void setReportedUser(User reportedUser) {
		this.reportedUser = reportedUser;
	}

	// ✅ GETTERS/SETTERS CHO FIELDS MỚI
	public String getAdminResponse() {
		return adminResponse;
	}

	public void setAdminResponse(String adminResponse) {
		this.adminResponse = adminResponse;
	}

	public LocalDateTime getResolvedAt() {
		return resolvedAt;
	}

	public void setResolvedAt(LocalDateTime resolvedAt) {
		this.resolvedAt = resolvedAt;
	}

	public User getResolvedBy() {
		return resolvedBy;
	}

	public void setResolvedBy(User resolvedBy) {
		this.resolvedBy = resolvedBy;
	}

}
