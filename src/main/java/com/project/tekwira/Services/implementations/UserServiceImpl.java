package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.ApiResponse;
import com.project.tekwira.Dto.SignupRequest;
import com.project.tekwira.Dto.UpdatePasswordRequest;
import com.project.tekwira.Dto.UpdateUserDto;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.PasswordResetTokenRepository;
import com.project.tekwira.Repositories.PlayerProfileRepository;
import com.project.tekwira.Repositories.UserRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.EmailService;
import com.project.tekwira.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerProfileRepository playerProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           PlayerProfileRepository playerProfileRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerProfileRepository = playerProfileRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public User signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalStateException(
                    "Email '" + signupRequest.getEmail() + "' is already in use."
            );
        }

        // Check phone uniqueness (only if provided)
        if (signupRequest.getPhoneNumber() != null && !signupRequest.getPhoneNumber().isEmpty()) {
            if (userRepository.existsByPhoneNumber(signupRequest.getPhoneNumber())) {
                throw new IllegalStateException(
                        "Phone number '" + signupRequest.getPhoneNumber() + "' is already in use."
                );
            }
        }
//        long otp = (long) (Math.random() * 900000) + 100000;
        var user = User.builder()
                .email(signupRequest.getEmail())
                .firstName(signupRequest.getFirstName())
                .lastName(signupRequest.getLastName())
                .phoneNumber(signupRequest.getPhoneNumber()) // can be null now
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .role(Role.PLAYER)
                .build();

       User savedUser = userRepository.save(user);

        PlayerProfile profile = new PlayerProfile();
        profile.setUser(savedUser);
        profile.setStrongFoot(StrongFoot.RIGHT); // default value
        profile.setPosition(Position.MID);       // default value
        profile.setGoals(0);
        profile.setReservationCount(0);
        profile.setBio("");                       // optional, can be empty
        profile.setProfilePicture(null);          // optional

        profile.setReliabilityScore(50);
        profile.setCompletedReservations(0);
        profile.setCancelledReservations(0);
        profile.setNoShowReservations(0);
        playerProfileRepository.save(profile);
//        String subject = "Your Email Verification OTP";
//        String message = "Hello " + signupRequest.getFullName() + ",\n\nYour OTP is: " + otp + "\n\nThank you!";
//        try {
//            emailService.sendEmail(savedUser.getEmail(), subject, message);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to send verification email to " + savedUser.getEmail(), e);
//        }
        return savedUser;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "User with email '" + email + "' not found"
                ));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }


    @Override
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusHours(1))
            .used(false)
            .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink =
            "http://localhost:4200/reset-password?token=" + token;

        emailService.sendEmail(
            user.getEmail(),
            "Reset Password",
            "Click here to reset your password:\n" + resetLink
        );
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<?>> resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken =
            passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);

        return ResponseEntity.ok(
            ApiResponse.builder()
                .message("Password updated successfully")
                .data(null)
                .build()
        );
    }

    @Override
    public List<User> getAllPlayers() {
        return userRepository.findByRole(Role.PLAYER);
    }

    @Override
    public List<User> getAllOwners() {
        return userRepository.findByRole(Role.OWNER);
    }

    @Override
    public User updateUser(Long userId, UpdateUserDto dto) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        /* ================= EMAIL ================= */

        if (dto.getEmail() != null &&
            !dto.getEmail().isBlank() &&
            !dto.getEmail().equals(user.getEmail())) {

            Optional<User> existingEmail =
                userRepository.findByEmail(dto.getEmail());

            if (existingEmail.isPresent()) {
                throw new RuntimeException("Email already exists");
            }

            user.setEmail(dto.getEmail());
        }

        /* ================= USERNAME ================= */

        if (dto.getUsername() != null &&
            !dto.getUsername().isBlank() &&
            !dto.getUsername().equals(user.getUsername())) {

            Optional<User> existingUsername =
                userRepository.findByUsername(dto.getUsername());

            if (existingUsername.isPresent()) {
                throw new RuntimeException("Username already exists");
            }

            user.setUsername(dto.getUsername());
        }

        /* ================= PHONE ================= */

        if (dto.getPhoneNumber() != null &&
            !dto.getPhoneNumber().isBlank() &&
            !dto.getPhoneNumber().equals(user.getPhoneNumber())) {

            Optional<User> existingPhone =
                userRepository.findByPhoneNumber(dto.getPhoneNumber());

            if (existingPhone.isPresent()) {
                throw new RuntimeException("Phone number already exists");
            }

            user.setPhoneNumber(dto.getPhoneNumber());
        }

        /* ================= FIRST NAME ================= */

        if (dto.getFirstName() != null &&
            !dto.getFirstName().isBlank()) {

            user.setFirstName(dto.getFirstName());
        }

        /* ================= LAST NAME ================= */

        if (dto.getLastName() != null &&
            !dto.getLastName().isBlank()) {

            user.setLastName(dto.getLastName());
        }

        return userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        User currentUser = JwtUtil.getCurrentUser();

        return userRepository.findById(currentUser.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void updatePassword(UpdatePasswordRequest request) {

        User currentUser = JwtUtil.getCurrentUser();

        User user = userRepository.findById(currentUser.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
            request.getCurrentPassword(),
            user.getPassword())) {

            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        user.setPassword(
            passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);
    }

}
