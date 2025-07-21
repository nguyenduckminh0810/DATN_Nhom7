package com.nhom7.quiz.quizapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nhom7.quiz.quizapp.model.Report;

public interface ReportRepo extends JpaRepository<Report, Long> {

}
