package com.project.tekwira.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OwnerRevenueDto {
    private Long ownerId;
    private String ownerName;
    private double revenue;
    private long stadiumCount;
}
