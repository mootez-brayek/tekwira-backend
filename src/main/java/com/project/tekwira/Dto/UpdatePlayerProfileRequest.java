package com.project.tekwira.Dto;

import com.project.tekwira.Models.Position;
import com.project.tekwira.Models.StrongFoot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePlayerProfileRequest {

    private String username;

    private String firstName;

    private String lastName;

    private String phoneNumber; // optional

    // --- PlayerProfile fields ---

    private String profilePicture;

    private StrongFoot strongFoot;

    private Position position;

    private String bio;
}
