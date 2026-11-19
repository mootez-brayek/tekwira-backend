package com.project.tekwira.Dto;

import com.project.tekwira.Models.NoShowReportStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoShowReportResponse {

    private Long id;

    private Long reservationId;

    private String playerName;

    private String ownerName;

    private String reason;

    private String playerDisputeReason;

    private NoShowReportStatus status;

    private LocalDateTime reportedAt;

    private LocalDateTime disputedAt;

    private LocalDateTime reviewedAt;
}
