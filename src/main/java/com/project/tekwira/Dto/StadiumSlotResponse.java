package com.project.tekwira.Dto;

import com.project.tekwira.Models.BookingType;
import com.project.tekwira.Models.ReservationStatus;
import com.project.tekwira.Models.ReservationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StadiumSlotResponse {
    private Long stadiumSlotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean reserved;

    private String playerName;           // Player who booked
    private BookingType reservationFrom;

    private Long reservationId;
    private ReservationType reservationType;
    private ReservationStatus reservationStatus;
    private String subscriptionId;
}
