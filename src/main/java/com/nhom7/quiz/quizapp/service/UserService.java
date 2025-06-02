package com.nhom7.quiz.quizapp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhom7.quiz.quizapp.model.User;
import com.nhom7.quiz.quizapp.repository.UserRepo;
@Service
public class UserService {
	@Autowired
	private UserRepo userRepo;
	
	public List<User> getAllUser(){
		return userRepo.findAll();
	}
}
