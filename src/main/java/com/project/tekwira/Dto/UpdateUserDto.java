package com.project.tekwira.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserDto {

    private String firstName;

    private String lastName;

    private String username;

    @Email
    private String email;

    private String phoneNumber;
}
