package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.ApiResponse;
import com.project.tekwira.Dto.AuthRequest;
import com.project.tekwira.Dto.LoginResponse;
import com.project.tekwira.Dto.SignupRequest;
import com.project.tekwira.Models.User;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signup(@RequestBody @Valid SignupRequest request) {
        try {
            User user = userService.signup(request);
            String accessToken = jwtUtil.generateAccessToken(user);

            LoginResponse loginResponse = new LoginResponse(
                    user.getEmail(),
                    accessToken,
                    user.getUserId(),
                    user.getRole(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber(),
                    user.getUsername()
            );

            ApiResponse<LoginResponse> response =
                    new ApiResponse<>("Account created successfully", loginResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException ex) {
            ApiResponse<LoginResponse> errorResponse =
                    new ApiResponse<>(ex.getMessage(), null);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception ex) {
            ApiResponse<LoginResponse> errorResponse =
                    new ApiResponse<>("An unexpected error occurred", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            User user = userService.findByEmail(request.getEmail());
            String accessToken = jwtUtil.generateAccessToken(user);

            LoginResponse data = new LoginResponse(
                    user.getEmail(),
                    accessToken,
                    user.getUserId(),
                    user.getRole(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber(),
                    user.getUsername()
            );

            return ResponseEntity.ok(new ApiResponse<>("Login successful", data));

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("Invalid credentials", null));
        }
    }
}
