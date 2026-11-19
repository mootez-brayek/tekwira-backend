package com.project.tekwira.Dto;

import com.project.tekwira.Models.ReservationStatus;
import com.project.tekwira.Models.ReservationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @NotNull(message = "Stadium ID is required")
    private Long stadiumId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    @NotNull
    private LocalDate date;

    // Optional: initial status for admin override (default can be PENDING)
    private ReservationStatus status = ReservationStatus.BOOKED;

    private ReservationType type;
}
