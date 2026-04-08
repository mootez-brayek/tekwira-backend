package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.ReservationRequest;
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
    public ResponseEntity<List<Reservation>> createReservation(@RequestBody ReservationRequest request) {
        List<Reservation> reservations = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservations);
    }

    // ❌ Player: Cancel a reservation by slot + date
    @DeleteMapping("/slot/{slotId}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        reservationService.cancelReservation(slotId, date);
        return ResponseEntity.noContent().build();
    }

    // 📖 Player: List all own reservations
    @GetMapping("/me")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<List<Reservation>> listMyReservations() {
        List<Reservation> reservations = reservationService.listReservationsForPlayer();
        return ResponseEntity.ok(reservations);
    }

    // 📖 Admin/Owner: List all reservations for a stadium
    @GetMapping("/stadium/{stadiumId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<Reservation>> listReservationsForStadium(@PathVariable Long stadiumId) {
        List<Reservation> reservations = reservationService.listReservationsForStadium(stadiumId);
        return ResponseEntity.ok(reservations);
    }

    // 📖 Get reservation by ID
    @GetMapping("/{reservationId}")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(reservation);
    }
}
