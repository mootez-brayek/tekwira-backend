package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.CreateOWnerRequest;
import com.project.tekwira.Models.Role;
import com.project.tekwira.Models.User;
import com.project.tekwira.Repositories.UserRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public User createOwner(CreateOWnerRequest request) {
        User principalUser = JwtUtil.getCurrentUser();

        // 2️⃣ Fetch managed entity from database
        User user = userRepository.findById(principalUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (! user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Only admins can create owner");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 2️⃣ Create Owner user
        User owner = new User();
        owner.setEmail(request.getEmail());
        owner.setUsername(request.getUsername());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setPhoneNumber(request.getPhoneNumber());
        owner.setRole(Role.OWNER);

        return userRepository.save(owner);
    }
}