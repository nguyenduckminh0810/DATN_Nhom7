package com.nhom7.quiz.quizapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nhom7.quiz.quizapp.model.Category;
import com.nhom7.quiz.quizapp.service.CategoryService;

@RestController
@RequestMapping("/category")
public class CategoryController {
	@Autowired
	private CategoryService categoryService;
	
	@GetMapping("all-category")
	public List<Category> getAllCategory() {
        return categoryService.getAllCategory();
    }
}
