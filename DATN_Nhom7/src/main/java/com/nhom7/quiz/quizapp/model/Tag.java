package com.nhom7.quiz.quizapp.model;

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
@Table(name = "tags")
public class Tag {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 50, nullable = false, unique = true)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@JsonIgnore
	@OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<QuizTag> quizTags = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<QuizTag> getQuizTags() {
		return quizTags;
	}

	public void setQuizTags(Set<QuizTag> quizTags) {
		this.quizTags = quizTags;
	}

	public Tag(Long id, String name, String description, Set<QuizTag> quizTags) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.quizTags = quizTags;
	}

	public Tag() {
		super();
	}

}
