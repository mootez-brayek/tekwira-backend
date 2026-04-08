package com.project.tekwira.Services;

import com.project.tekwira.Dto.ReservationRequest;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.StadiumSlot;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    List<Reservation> createReservation(ReservationRequest request);

    // Cancel a reservation by slot and date
    void cancelReservation(Long slotId, LocalDate date);

    // List all reservations for the current player
    List<Reservation> listReservationsForPlayer();

    // List all reservations for a stadium (admin)
    List<Reservation> listReservationsForStadium(Long stadiumId);

    // Get a reservation by its ID
    Reservation getReservationById(Long reservationId);
}
