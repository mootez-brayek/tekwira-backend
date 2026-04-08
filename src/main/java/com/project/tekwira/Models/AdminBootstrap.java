package com.project.tekwira.Models;

import com.project.tekwira.Repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${superadmin.email}")
    private String email;

    @Value("${superadmin.username}")
    private String username;

    @Value("${superadmin.password}")
    private String password;

    @Value("${superadmin.firstname}")
    private String firstName;

    @Value("${superadmin.lastname}")
    private String lastName;


    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            User admin = new User();
            admin.setEmail(email);
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setFirstName(firstName);
            admin.setLastName(lastName);
            admin.setRole(Role.ADMIN);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            System.out.println("✅ Default SUPER_ADMIN created");
        }
    }
}
