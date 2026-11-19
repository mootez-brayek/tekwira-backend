package com.project.tekwira.Services;

import com.project.tekwira.Dto.ApiResponse;
import com.project.tekwira.Dto.SignupRequest;
import com.project.tekwira.Dto.UpdatePasswordRequest;
import com.project.tekwira.Dto.UpdateUserDto;
import com.project.tekwira.Models.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    User signup(SignupRequest signupRequest);
    User findByEmail(String email);
    List<User> findAll();
    void forgotPassword(String email);
    ResponseEntity<ApiResponse<?>> resetPassword(String token, String newPassword);
    List<User> getAllPlayers();
    List<User> getAllOwners();
    User updateUser(Long userId, UpdateUserDto dto);
    User getCurrentUser();
    User getUserById(Long userId);

    void updatePassword(UpdatePasswordRequest request);
}
