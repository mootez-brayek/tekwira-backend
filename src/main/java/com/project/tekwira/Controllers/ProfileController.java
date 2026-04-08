package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.UpdatePlayerProfileRequest;
import com.project.tekwira.Models.PlayerProfile;
import com.project.tekwira.Models.User;
import com.project.tekwira.Services.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping("/update")
    public ResponseEntity<PlayerProfile> updateProfile(
            @RequestBody @Valid UpdatePlayerProfileRequest request
    ) {
        PlayerProfile updatedProfile = profileService.updateFullProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }
}
