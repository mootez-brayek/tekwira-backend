package com.project.tekwira.Dto;

import com.project.tekwira.Models.BookingType;
import com.project.tekwira.Models.FieldType;
import com.project.tekwira.Models.ReservationStatus;
import com.project.tekwira.Models.ReservationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationResponse {

    private Long id;
    private Long stadiumId;
    private LocalDate date;
    private String stadiumName;
    private String username;
    private LocalTime startTime;
    private LocalTime endTime;
    private FieldType fieldType;
    private Long slotId;
    private BookingType reservationFrom;
    private ReservationType reservationType;
    private ReservationStatus reservationStatus;
    private String subscriptionId;
}
