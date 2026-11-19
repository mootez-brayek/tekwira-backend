package com.project.tekwira.Services;

import com.project.tekwira.Dto.ProfileResponse;
import com.project.tekwira.Dto.UpdatePlayerProfileRequest;
import com.project.tekwira.Models.PlayerProfile;

public interface ProfileService {
    PlayerProfile updateFullProfile(UpdatePlayerProfileRequest request);

    ProfileResponse getMyProfile();
}
