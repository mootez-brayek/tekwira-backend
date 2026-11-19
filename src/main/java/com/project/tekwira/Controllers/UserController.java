package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.*;
import com.project.tekwira.Models.User;
import com.project.tekwira.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
        @RequestBody ForgotPasswordRequest request
    ) {
        userService.forgotPassword(request.getEmail());

        return ResponseEntity.ok(
            ApiResponse.builder()
                .message("Reset link sent successfully")
                .data(null)
                .build()
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
        @RequestBody ResetPasswordRequest request
    ) {
        return userService.resetPassword(
            request.getToken(),
            request.getNewPassword()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(
        @PathVariable Long id,
        @RequestBody @Valid UpdateUserDto dto
    ) {

        return ResponseEntity.ok(
            userService.updateUser(id, dto)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            userService.getUserById(id)
        );
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
        @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(request);
        return ResponseEntity.ok().build();
    }
}
