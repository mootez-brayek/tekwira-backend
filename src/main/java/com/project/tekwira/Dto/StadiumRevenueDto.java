package com.project.tekwira.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StadiumRevenueDto {

    private Long stadiumId;
    private String stadiumName;
    private String ownerName;
    private double revenue;
    private long totalBookings;
    private long oneGameBookings;

    private long subscriptionBookings;

    private Long ownerId;
}
