package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.ReservationRequest;
import com.project.tekwira.Dto.ReservationResponse;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.StadiumSlot;
import com.project.tekwira.Services.ReservationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<ReservationResponse>> createReservation(@RequestBody ReservationRequest request) {
        List<ReservationResponse> reservations = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservations);
    }

    // ❌ Player: Cancel a reservation by slot + date
    @PutMapping("/{reservationId}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long reservationId) {
        ReservationResponse cancelledReservation =
            reservationService.cancelReservation(reservationId);

        return ResponseEntity.ok(cancelledReservation);
    }

    // 📖 Player: List all own reservations
    @GetMapping("/me")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<ReservationResponse>> listMyReservations() {
        List<ReservationResponse> reservations = reservationService.listReservationsForPlayer();
        return ResponseEntity.ok(reservations);
    }

    // 📖 Admin/Owner: List all reservations for a stadium
    @GetMapping("/stadium/{stadiumId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ReservationResponse>> listReservationsForStadium(@PathVariable Long stadiumId) {
        List<ReservationResponse> reservations = reservationService.listReservationsForStadium(stadiumId);
        return ResponseEntity.ok(reservations);
    }

    // 📖 Get reservation by ID
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long reservationId) {
        ReservationResponse reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('OWNER')")
    public List<ReservationResponse> getReservationHistory(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return reservationService.getReservationsByDateRange(from, to);
    }

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<ReservationResponse>> getMyUpcomingReservations() {
        List<ReservationResponse> reservations =
            reservationService.myUpcomingReservation();
        return ResponseEntity.ok(reservations);
    }

    @PutMapping("/{reservationId}/complete")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ReservationResponse> completeReservation(
        @PathVariable Long reservationId) {

        ReservationResponse completedReservation =
            reservationService.completeReservation(reservationId);

        return ResponseEntity.ok(
            reservationService.getReservationById(completedReservation.getId())
        );
    }

    // Owner: Cancel a direct booking
    @PutMapping("/{reservationId}/owner-cancel")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ReservationResponse> ownerCancelReservation(
        @PathVariable Long reservationId) {

        ReservationResponse cancelledReservation =
            reservationService.ownerCancelReservation(reservationId);

        return ResponseEntity.ok(cancelledReservation);
    }
}
