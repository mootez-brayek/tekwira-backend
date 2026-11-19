package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.ReservationResponse;
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
import java.time.LocalTime;
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

        List<StadiumSlotResponse> responses = new ArrayList<>();

        LocalTime opening = stadium.getOpeningTime();
        LocalTime closing = stadium.getClosingTime();

        int startMinutes = opening.getHour() * 60 + opening.getMinute();
        int endMinutes = closing.getHour() * 60 + closing.getMinute();

        // overnight case
        if (endMinutes <= startMinutes) {
            endMinutes += 24 * 60;
        }

        int slotDuration = 90;

        for (int current = startMinutes; current + slotDuration <= endMinutes; current += slotDuration) {

            int next = current + slotDuration;

            LocalTime startTime = LocalTime.of(
                (current / 60) % 24,
                current % 60
            );

            LocalTime endTime = LocalTime.of(
                (next / 60) % 24,
                next % 60
            );

            StadiumSlot slot = StadiumSlot.builder()
                .stadium(stadium)
                .startTime(startTime)
                .endTime(endTime)
                .build();

            StadiumSlot saved = stadiumSlotRepository.save(slot);

            responses.add(new StadiumSlotResponse(
                saved.getId(),
                saved.getStartTime(),
                saved.getEndTime(),
                false,
                null,
                null,
                null,
                null,
                null,
                null
            ));
        }

        return responses;
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
                    Reservation reservation = reservationOpt.orElse(null);

                    return new StadiumSlotResponse(
                            slot.getId(),
                            slot.getStartTime(),
                            slot.getEndTime(),
                            isReserved,
                            isReserved ? reservation.getPlayer().getUsername() : null,
                            isReserved ? reservation.getReservationFrom() : null,
                            isReserved ? reservation.getId() : null,
                            isReserved ? reservation.getReservationType() : null,
                            isReserved ? reservation.getStatus() : null,
                            isReserved ? reservation.getSubscriptionId() : null
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
        Reservation reservation = reservationOpt.orElse(null);

        return new StadiumSlotResponse(
                slot.getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                isReserved,
                isReserved ? reservation.getPlayer().getUsername() : null,
                isReserved ? reservation.getReservationFrom() : null,
                isReserved ? reservation.getId() : null,
                isReserved ? reservation.getReservationType() : null,
                isReserved ? reservation.getStatus() : null,
                isReserved ? reservation.getSubscriptionId() : null
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
    public ReservationResponse markSlotDirectBooked(Long stadiumId, Long slotId, LocalDate date) {
        User owner = JwtUtil.getCurrentUser();

        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new RuntimeException("Stadium not found"));

        if (!stadium.getOwner().getUserId().equals(owner.getUserId())) {
            throw new RuntimeException("You can only book slots for your own stadiums");
        }

        StadiumSlot slot = stadiumSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // Check if already booked
        boolean exists = reservationRepository.existsBySlotAndDateAndStatus(
            slot,
            date,
            ReservationStatus.BOOKED
        );
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
        reservation.setStatus(ReservationStatus.BOOKED);
        reservation.setReservationType(ReservationType.ONE_GAME);
        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToResponse(savedReservation);
    }

    // ====================

    private StadiumSlotResponse mapToResponse(StadiumSlot slot, LocalDate date) {

        Optional<Reservation> reservationOpt =
            reservationRepository.findBySlotAndDateAndStatus(
                slot,
                date,
                ReservationStatus.BOOKED
            );

        boolean isReserved = reservationOpt.isPresent();

        Reservation reservation = reservationOpt.orElse(null);

        String playerName = isReserved
            ? reservation.getPlayer().getUsername()
            : null;

        BookingType reservationFrom = isReserved
            ? reservation.getReservationFrom()
            : null;

        return new StadiumSlotResponse(
            slot.getId(),
            slot.getStartTime(),
            slot.getEndTime(),
            isReserved,
            playerName,
            reservationFrom,
            isReserved ? reservation.getId() : null,
            isReserved ? reservation.getReservationType() : null,
            isReserved ? reservation.getStatus() : null,
            isReserved ? reservation.getSubscriptionId() : null
        );
    }



    private ReservationResponse mapToResponse(Reservation r) {
        return new ReservationResponse(
            r.getId(),
            r.getStadium().getId(),
            r.getDate(),
            r.getStadium().getName(),
            r.getPlayer().getUsername(),
            r.getStartTime(),
            r.getEndTime(),
            r.getStadium().getFieldType(),
            r.getSlot().getId(),
            r.getReservationFrom(),
            r.getReservationType(),
            r.getStatus(),
            r.getSubscriptionId()
        );
    }
}
