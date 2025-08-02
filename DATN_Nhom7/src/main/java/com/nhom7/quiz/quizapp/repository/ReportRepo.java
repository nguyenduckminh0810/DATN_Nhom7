package com.nhom7.quiz.quizapp.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nhom7.quiz.quizapp.model.Report;

public interface ReportRepo extends JpaRepository<Report, Long> {

        Page<Report> findByStatus(String status, Pageable pageable);

        List<Report> findByUser_Id(Long userId);

        List<Report> findByQuiz_Id(Long quizId);

        long countByStatus(String status);

        int countByReportedUserIdAndStatus(Long userId, String status);

        @Query("SELECT r FROM Report r WHERE r.quiz.id = :quizId AND r.user.id = :userId")
        List<Report> findByQuizIdAndUserId(@Param("quizId") Long quizId,
                        @Param("userId") Long userId);

        @Query("Select r.status, count(r) from Report r group by r.status")
        List<Object[]> getReportStatusCount();

}
