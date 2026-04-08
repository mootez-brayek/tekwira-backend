package com.project.tekwira.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OwnerDashboardStatsDto {

    private int totalStadiums;
    private int totalBookingsToday;
    private double revenue;
    private int appReservationsToday;
    private int directReservationsToday;
}
