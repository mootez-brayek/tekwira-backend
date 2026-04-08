package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.StadiumSlotResponse;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Repositories.StadiumSlotRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.StadiumSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StadiumSlotImpl implements StadiumSlotService {
    private final StadiumRepository stadiumRepository;
    private final StadiumSlotRepository stadiumSlotRepository;
    private final ReservationRepository reservationRepository;

    public StadiumSlotImpl(StadiumRepository stadiumRepository, StadiumSlotRepository stadiumSlotRepository, ReservationRepository reservationRepository) {
        this.stadiumRepository = stadiumRepository;
        this.stadiumSlotRepository = stadiumSlotRepository;
        this.reservationRepository = reservationRepository;
    }

    // ====================
    @Override
    @Transactional
    public List<StadiumSlotResponse> createSlotsForStadium(Long stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new RuntimeException("Stadium not found"));

        List<StadiumSlot> slots = new ArrayList<>();
        var startTime = stadium.getOpeningTime();
        var endTime = stadium.getClosingTime();

        while (!startTime.isAfter(endTime.minusMinutes(90))) {
            StadiumSlot slot = StadiumSlot.builder()
                    .stadium(stadium)
                    .startTime(startTime)
                    .endTime(startTime.plusMinutes(90))
                    .build();
            slots.add(slot);
            startTime = startTime.plusMinutes(90);
        }
        List<StadiumSlot> saved = stadiumSlotRepository.saveAll(slots);

        // For creation, reserved is always false
        return saved.stream()
                .map(slot -> new StadiumSlotResponse(slot.getId(), slot.getStartTime(), slot.getEndTime(), false, null, null))
                .toList();
    }

    // ====================
    @Override
    public List<StadiumSlotResponse> listSlotsByStadium(Long stadiumId) {
        List<StadiumSlot> slots = stadiumSlotRepository.findByStadiumId(stadiumId);

        // Map each slot and include reservation info if exists
        return slots.stream()
                .map(slot -> {
                    // Find reservation for this slot (latest or for today, depending on your logic)
                    Optional<Reservation> reservationOpt = reservationRepository.findBySlot(slot);

                    boolean isReserved = reservationOpt.isPresent();
                    String playerName = isReserved ? reservationOpt.get().getPlayer().getUsername() : null;
                    BookingType reservationFrom = isReserved ? reservationOpt.get().getReservationFrom() : null;

                    return new StadiumSlotResponse(
                            slot.getId(),
                            slot.getStartTime(),
                            slot.getEndTime(),
                            isReserved,
                            playerName,
                            reservationFrom
                    );
                })
                .toList();
    }

    @Override
    public StadiumSlotResponse getSlotById(Long slotId) {
        StadiumSlot slot = stadiumSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Find reservation for this slot (you can also filter by date if needed)
        Optional<Reservation> reservationOpt = reservationRepository.findBySlot(slot);

        boolean isReserved = reservationOpt.isPresent();
        String playerName = isReserved ? reservationOpt.get().getPlayer().getUsername() : null;
        BookingType reservationFrom = isReserved ? reservationOpt.get().getReservationFrom() : null;

        return new StadiumSlotResponse(
                slot.getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                isReserved,
                playerName,
                reservationFrom
        );
    }

    // ====================
    @Override
    public void deleteSlot(Long slotId) {
        StadiumSlot slot = stadiumSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        stadiumSlotRepository.delete(slot);
    }

    // ====================
    @Override
    public List<StadiumSlotResponse> getSlotsForDate(Long stadiumId, LocalDate date) {
        List<StadiumSlot> templates = stadiumSlotRepository.findByStadiumId(stadiumId);

        // Map each slot and include player and booking type if reserved
        return templates.stream()
                .map(slot -> mapToResponse(slot, date))
                .toList();
    }

    // ====================
    @Override
    @Transactional
    public Reservation markSlotDirectBooked(Long stadiumId, Long slotId, LocalDate date) {
        User owner = JwtUtil.getCurrentUser();

        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new RuntimeException("Stadium not found"));

        if (!stadium.getOwner().getUserId().equals(owner.getUserId())) {
            throw new RuntimeException("You can only book slots for your own stadiums");
        }

        StadiumSlot slot = stadiumSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Check if already booked
        boolean exists = reservationRepository.existsBySlotAndDate(slot, date);
        if (exists) {
            throw new RuntimeException("Slot is already booked for this date");
        }

        Reservation reservation = new Reservation();
        reservation.setPlayer(owner); // Admin can act as player for direct booking
        reservation.setStadium(stadium);
        reservation.setSlot(slot);
        reservation.setDate(date);
        reservation.setStartTime(slot.getStartTime());
        reservation.setEndTime(slot.getEndTime());
        reservation.setReservationFrom(BookingType.DIRECT);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return reservationRepository.save(reservation);
    }

    // ====================
    private StadiumSlotResponse mapToResponse(StadiumSlot slot, LocalDate date) {
        // Find the reservation for this slot and date (if exists)
        Optional<Reservation> reservationOpt = reservationRepository.findBySlotAndDate(slot, date);

        boolean isReserved = reservationOpt.isPresent();
        String playerName = isReserved ? reservationOpt.get().getPlayer().getUsername() : null;
        BookingType reservationFrom = isReserved ? reservationOpt.get().getReservationFrom() : null;

        return new StadiumSlotResponse(
                slot.getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                isReserved,
                playerName,
                reservationFrom
        );
    }
}