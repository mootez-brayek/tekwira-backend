package com.project.tekwira.Dto;

import com.project.tekwira.Models.ReliabilityEventType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReliabilityTransactionResponse {
    private Long id;

    private Long reservationId;

    private ReliabilityEventType eventType;

    private int pointsChange;

    private int previousScore;

    private int newScore;

    private LocalDateTime createdAt;
}
