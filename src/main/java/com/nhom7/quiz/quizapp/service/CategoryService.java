package com.nhom7.quiz.quizapp.service;

import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.repository.CategoryRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
	@Autowired
    private CategoryRepo categoryRepo;

    public Category save(Category category) {
    	return categoryRepo.save(category);
    }

    public List<Category> getAllCategory() {
        return categoryRepo.findAll();
    }
}
