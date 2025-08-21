package com.nhom7.quiz.quizapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "quiz_id", nullable = false)
	private Quiz quiz;

	@Column(nullable = false)
	private int score;

	@Column(name = "attempted_at", nullable = false)
	private LocalDateTime attemptedAt = LocalDateTime.now();

	@Column(name = "time_taken", nullable = false)
	private int timeTaken; // đơn vị giây
	
	// ✅ THÊM TRẠNG THÁI ATTEMPT
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private AttemptStatus status = AttemptStatus.IN_PROGRESS;
	
	// ✅ THÊM RELATIONSHIP VỚI PROGRESS
	@OneToOne(mappedBy = "attempt", cascade = jakarta.persistence.CascadeType.ALL, fetch = jakarta.persistence.FetchType.LAZY)
	private QuizAttemptProgress progress;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Quiz getQuiz() {
		return this.quiz;
	}

	public void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	public int getScore() {
		return this.score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTimeTaken() {
		return this.timeTaken;
	}

	public void setTimeTaken(int timeTaken) {
		this.timeTaken = timeTaken;
	}

	public LocalDateTime getAttemptedAt() {
		return this.attemptedAt;
	}

	public void setAttemptedAt(LocalDateTime attemptedAt) {
		this.attemptedAt = attemptedAt;
	}
	
	// ✅ GETTERS VÀ SETTERS CHO STATUS
	public AttemptStatus getStatus() {
		return status;
	}

	public void setStatus(AttemptStatus status) {
		this.status = status;
	}
	
	// ✅ GETTERS VÀ SETTERS CHO PROGRESS
	public QuizAttemptProgress getProgress() {
		return progress;
	}

	public void setProgress(QuizAttemptProgress progress) {
		this.progress = progress;
	}

	public QuizAttempt(Long id, User user, Quiz quiz, int score, LocalDateTime attemptedAt, int timeTaken) {
		super();
		this.id = id;
		this.user = user;
		this.quiz = quiz;
		this.score = score;
		this.attemptedAt = attemptedAt;
		this.timeTaken = timeTaken;
		this.status = AttemptStatus.IN_PROGRESS;
	}

	public QuizAttempt() {
		super();
		this.status = AttemptStatus.IN_PROGRESS;
	}
	
	// ✅ HELPER METHODS
	public boolean isInProgress() {
		return AttemptStatus.IN_PROGRESS.equals(this.status);
	}
	
	public boolean isSubmitted() {
		return AttemptStatus.SUBMITTED.equals(this.status);
	}
	
	public boolean isCompleted() {
		return AttemptStatus.COMPLETED.equals(this.status);
	}
	
	public boolean isAbandoned() {
		return AttemptStatus.ABANDONED.equals(this.status);
	}
	
	public void markAsSubmitted() {
		this.status = AttemptStatus.SUBMITTED;
	}
	
	public void markAsCompleted() {
		this.status = AttemptStatus.COMPLETED;
	}
	
	public void markAsAbandoned() {
		this.status = AttemptStatus.ABANDONED;
		if (this.progress != null) {
			this.progress.markAsInactive();
		}
	}
}
