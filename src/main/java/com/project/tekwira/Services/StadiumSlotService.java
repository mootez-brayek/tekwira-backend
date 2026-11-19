package com.project.tekwira.Services;

import com.project.tekwira.Dto.ReservationResponse;
import com.project.tekwira.Dto.StadiumSlotResponse;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.StadiumSlot;

import java.time.LocalDate;
import java.util.List;

public interface StadiumSlotService {

    // Create template slots for a stadium
    List<StadiumSlotResponse> createSlotsForStadium(Long stadiumId);

    // Get template slots for a stadium
    List<StadiumSlotResponse> listSlotsByStadium(Long stadiumId);

    // Get a single slot by ID
    StadiumSlotResponse getSlotById(Long slotId);

    // Delete a template slot
    void deleteSlot(Long slotId);

    // Get slots for a given stadium and date with availability info
    List<StadiumSlotResponse> getSlotsForDate(Long stadiumId, LocalDate date);

    // Admin marks a slot as directly booked for a specific date
    ReservationResponse markSlotDirectBooked(Long stadiumId, Long slotId, LocalDate date);
}
