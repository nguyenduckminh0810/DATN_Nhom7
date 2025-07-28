package com.nhom7.quiz.quizapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "quizzes")
public class Quiz {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category;

	@Column(name = "is_public")
	private boolean isPublic;

	@Column(name = "created_at")
	private LocalDateTime createdAt = LocalDateTime.now();

	// ✅ THÊM FIELD IMAGE
	private String image;

	@JsonIgnore
	@OneToMany(mappedBy = "quiz")
	private Set<QuizTag> quizTags;
	
	// ✅ THÊM RELATIONSHIP VỚI QUESTIONS
	@JsonIgnore
	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Question> questions = new HashSet<>();
	
	// Sau này dùng
	@JsonIgnore
	@OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL)
	private Set<QuizReview> reviews = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Set<QuizTag> getQuizTags() {
		return quizTags;
	}

	public void setQuizTags(Set<QuizTag> quizTags) {
		this.quizTags = quizTags;
	}

	// ✅ GETTER/SETTER CHO IMAGE
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	// ✅ GETTER/SETTER CHO QUESTIONS
	public Set<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(Set<Question> questions) {
		this.questions = questions;
	}

	public Quiz(Long id, String title, User user, Category category, boolean isPublic, LocalDateTime createdAt,
			Set<QuizTag> quizTags, String image) {
		super();
		this.id = id;
		this.title = title;
		this.user = user;
		this.category = category;
		this.isPublic = isPublic;
		this.createdAt = createdAt;
		this.quizTags = quizTags;
		this.image = image;
		this.questions = new HashSet<>();
	}

	public Quiz() {
		super();
	}
}
