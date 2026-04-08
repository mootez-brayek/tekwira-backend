package com.project.tekwira.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopStadiumDto {
    private String name;
    private long bookings;
    private long appBookings;
    private long directBookings;
}
