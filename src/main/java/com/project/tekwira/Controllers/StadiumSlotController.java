package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.ReservationResponse;
import com.project.tekwira.Dto.StadiumSlotResponse;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.StadiumSlot;
import com.project.tekwira.Services.StadiumSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stadium-slots")
@CrossOrigin("*")
public class StadiumSlotController {

    private final StadiumSlotService stadiumSlotService;

    public StadiumSlotController(StadiumSlotService stadiumSlotService) {
        this.stadiumSlotService = stadiumSlotService;
    }

    // 🏟 OWNER: Create slots for a day
    // 🏟 OWNER: Create template slots for a stadium
    @PostMapping("/stadium/{stadiumId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<StadiumSlotResponse>> createSlotsForStadium(@PathVariable Long stadiumId) {
        List<StadiumSlotResponse> slots = stadiumSlotService.createSlotsForStadium(stadiumId);
        return ResponseEntity.status(HttpStatus.CREATED).body(slots);
    }

    // 🏟 OWNER: Directly book a slot for a date
    @PostMapping("/stadium/{stadiumId}/slot/{slotId}/book")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ReservationResponse> markSlotDirectBooked(
            @PathVariable Long stadiumId,
            @PathVariable Long slotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        ReservationResponse reservation = stadiumSlotService.markSlotDirectBooked(stadiumId, slotId, date);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    // 🏟 OWNER/PLAYER: Delete a template slot
    @DeleteMapping("/{slotId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        stadiumSlotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }

    // 📖 List all template slots for a stadium
    @GetMapping("/stadium/{stadiumId}")
    public ResponseEntity<List<StadiumSlotResponse>> listSlotsByStadium(@PathVariable Long stadiumId) {
        List<StadiumSlotResponse> slots = stadiumSlotService.listSlotsByStadium(stadiumId);
        return ResponseEntity.ok(slots);
    }

    // 📖 List available slots for a specific date
    @GetMapping("/stadium/{stadiumId}/date")
    public ResponseEntity<List<StadiumSlotResponse>> getSlotsForDate(
            @PathVariable Long stadiumId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<StadiumSlotResponse> slots = stadiumSlotService.getSlotsForDate(stadiumId, date);
        return ResponseEntity.ok(slots);
    }

    // 📖 Get slot by ID
    @GetMapping("/{slotId}")
    public ResponseEntity<StadiumSlotResponse> getSlotById(@PathVariable Long slotId) {
        StadiumSlotResponse slot = stadiumSlotService.getSlotById(slotId);
        return ResponseEntity.ok(slot);
    }
}
