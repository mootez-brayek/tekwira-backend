package com.project.tekwira.Dto;

import com.project.tekwira.Models.Position;
import com.project.tekwira.Models.StrongFoot;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileResponse {
    private Long id;

    private String username;
    private String email;

    private String firstName;
    private String lastName;
    private StrongFoot strongFoot;
    private int reservationCount;
    private Position position;
    private String phoneNumber;
    private String bio;
    // optional later
    private String imageUrl;
}
