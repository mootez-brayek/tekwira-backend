package com.project.tekwira.Dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerReliabilityResponse {

    private int reliabilityScore;

    private int completedReservations;

    private int cancelledReservations;

    private int noShowReservations;
}
