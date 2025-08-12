package com.nhom7.quiz.quizapp.service;

import java.sql.Timestamp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.dto.ActivityDTO;
import com.nhom7.quiz.quizapp.repository.ActivityRepo;

@Service
public class ActivityService {
    private final ActivityRepo activityRepo;

    public ActivityService(ActivityRepo activityRepo) {
        this.activityRepo = activityRepo;
    }

    public Page<ActivityDTO> getRecentActivities(Pageable pageable) {
        Page<Object[]> pageData = activityRepo.getRecentActivities(pageable);

        return pageData.map(row -> new ActivityDTO(
                ((Number) row[0]).longValue(),
                (String) row[1],
                (String) row[2],
                ((Timestamp) row[3]).toLocalDateTime()));
    }
}
