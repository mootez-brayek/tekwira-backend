package com.project.tekwira.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 📅 Date of booking
    private LocalDate date;

    // ⏰ Time range
    private LocalTime startTime;
    private LocalTime endTime;

    private BookingType reservationFrom;
    private String subscriptionId;
    // 🔗 Relations
    @ManyToOne
    private User player;

    @ManyToOne
    private Stadium stadium;

    @ManyToOne(optional = false)
    private StadiumSlot slot;
    // 📌 Status
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    // 🕒 Auditing
    private LocalDateTime createdAt;
}
