package com.project.tekwira.Dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardStatsDto {

    private long totalUsers;
    private long totalStadiums;
    private long reservationsThisWeek;
    private long appReservationsThisWeek;
    private long directReservationsThisWeek;
    private List<TopStadiumDto> topStadiums;
    private List<TopPlayerDto> topPlayers;
}

