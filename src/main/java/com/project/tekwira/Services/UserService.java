package com.project.tekwira.Services;

import com.project.tekwira.Dto.SignupRequest;
import com.project.tekwira.Models.User;

import java.util.List;

public interface UserService {
    User signup(SignupRequest signupRequest);
    User findByEmail(String email);
    List<User> findAll();
}
