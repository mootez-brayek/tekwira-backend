package com.project.tekwira.Dto;

import com.project.tekwira.Models.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopPlayerDto {
    private String name;
    private long bookings;
}
