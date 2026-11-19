package com.project.tekwira.Services;

import com.project.tekwira.Dto.ReservationRequest;
import com.project.tekwira.Dto.ReservationResponse;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.StadiumSlot;

import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    List<ReservationResponse> createReservation(ReservationRequest request);

    // Cancel a reservation by slot and date
    ReservationResponse cancelReservation(Long reservationId);

    // List all reservations for the current player
    List<ReservationResponse> listReservationsForPlayer();

    // List all reservations for a stadium (admin)
    List<ReservationResponse> listReservationsForStadium(Long stadiumId);

    // Get a reservation by its ID
    ReservationResponse getReservationById(Long reservationId);

    List<ReservationResponse> getReservationsByDateRange(LocalDate start, LocalDate end);

    List<ReservationResponse> myUpcomingReservation();

    ReservationResponse completeReservation(Long reservationId);

    ReservationResponse ownerCancelReservation(Long reservationId);
}
