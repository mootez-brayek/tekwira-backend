package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.SignupRequest;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.PlayerProfileRepository;
import com.project.tekwira.Repositories.UserRepository;
import com.project.tekwira.Services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerProfileRepository playerProfileRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           PlayerProfileRepository playerProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerProfileRepository = playerProfileRepository;
    }

    @Override
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
        profile.setUser(user);
        profile.setStrongFoot(StrongFoot.RIGHT); // default value
        profile.setPosition(Position.MID);       // default value
        profile.setGoals(0);
        profile.setReservationCount(0);
        profile.setBio("");                       // optional, can be empty
        profile.setProfilePicture(null);          // optional

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


}
