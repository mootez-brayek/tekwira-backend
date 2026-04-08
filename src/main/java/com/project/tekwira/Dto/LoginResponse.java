package com.project.tekwira.Dto;

import com.project.tekwira.Models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String email;
    private String accessToken;
    private Long id;
    private Role role;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String username;

}
