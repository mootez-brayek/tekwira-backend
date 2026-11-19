package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.ProfileResponse;
import com.project.tekwira.Dto.UpdatePlayerProfileRequest;
import com.project.tekwira.Models.PlayerProfile;
import com.project.tekwira.Models.User;
import com.project.tekwira.Repositories.PlayerProfileRepository;
import com.project.tekwira.Repositories.UserRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PlayerProfileRepository profileRepository;

    public ProfileServiceImpl(UserRepository userRepository, PlayerProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    @Override
    public PlayerProfile updateFullProfile(UpdatePlayerProfileRequest request) {
        User principalUser = JwtUtil.getCurrentUser();

        // 2️⃣ Fetch managed entity from database
        User user = userRepository.findById(principalUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 2️⃣ Update User fields
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        userRepository.save(user);

        // 3️⃣ Update PlayerProfile fields if they exist in request
        PlayerProfile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.getProfilePicture() != null && !request.getProfilePicture().isBlank()) {
            profile.setProfilePicture(request.getProfilePicture());
        }
        if (request.getStrongFoot() != null) {
            profile.setStrongFoot(request.getStrongFoot());
        }
        if (request.getPosition() != null) {
            profile.setPosition(request.getPosition());
        }
        if (request.getBio() != null && !request.getBio().isBlank()) {
            profile.setBio(request.getBio());
        }

        return profileRepository.save(profile);
    }

    @Override
    public ProfileResponse getMyProfile() {
        User principalUser = JwtUtil.getCurrentUser();

        User user = userRepository.findById(principalUser.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        PlayerProfile profile = profileRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Profile not found"));
        return mapToDto(profile);
    }

    private ProfileResponse mapToDto(PlayerProfile profile) {
        ProfileResponse dto = new ProfileResponse();

        dto.setId(profile.getProfileId());
        dto.setStrongFoot(profile.getStrongFoot());
        dto.setPosition(profile.getPosition());
        dto.setReservationCount(profile.getReservationCount());
        dto.setBio(profile.getBio());
        // from User entity
        dto.setEmail(profile.getUser().getEmail());
        dto.setUsername(profile.getUser().getUsername());
        dto.setFirstName(profile.getUser().getFirstName());
        dto.setLastName(profile.getUser().getLastName());
        dto.setPhoneNumber(profile.getUser().getPhoneNumber());

        return dto;
    }
}
