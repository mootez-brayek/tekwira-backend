package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.ReservationRequest;
import com.project.tekwira.Dto.ReservationResponse;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.PlayerProfileRepository;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Repositories.StadiumSlotRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.PlayerReliabilityService;
import com.project.tekwira.Services.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final StadiumRepository stadiumRepository;
    private final ReservationRepository reservationRepository;
    private final StadiumSlotRepository stadiumSlotRepository;

    private final PlayerProfileRepository playerProfileRepository;

    private final PlayerReliabilityService playerReliabilityService;

    public ReservationServiceImpl(StadiumRepository stadiumRepository, ReservationRepository reservationRepository,
                                  StadiumSlotRepository stadiumSlotRepository, PlayerProfileRepository playerProfileRepository,
                                  PlayerReliabilityService playerReliabilityService) {
        this.stadiumRepository = stadiumRepository;
        this.reservationRepository = reservationRepository;
        this.stadiumSlotRepository = stadiumSlotRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.playerReliabilityService = playerReliabilityService;
    }

    @Override
    @Transactional
    public List<ReservationResponse> createReservation(ReservationRequest request) {

        User player = JwtUtil.getCurrentUser();
        PlayerProfile profile = playerProfileRepository.findByUser(player)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        Stadium stadium = stadiumRepository.findById(request.getStadiumId())
                .orElseThrow(() -> new RuntimeException("Stadium not found"));

        StadiumSlot slot = stadiumSlotRepository.findByIdWithLock(request.getSlotId())
            .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (request.getDate() == null) {
            throw new IllegalArgumentException("Reservation date is required.");
        }

        LocalDateTime reservationStart = LocalDateTime.of(
            request.getDate(),
            slot.getStartTime()
        );

        if (!reservationStart.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                "You cannot book a reservation for a past date or time."
            );
        }

        List<Reservation> reservations = new ArrayList<>();

        if (request.getType() == ReservationType.ONE_GAME) {

            boolean exists = reservationRepository.existsBySlotAndDateAndStatus(
                slot,
                request.getDate(),
                ReservationStatus.BOOKED
            );

            if (exists) {
                throw new RuntimeException("Slot already booked");
            }

            reservations.add(buildReservation(player, stadium, slot, request.getDate(), null,BookingType.APP, LocalDateTime.now(), request.getType()));

        } else if (request.getType() == ReservationType.SUBSCRIPTION) {

            String subscriptionId = UUID.randomUUID().toString();
            LocalDate currentDate = request.getDate();

            for (int i = 0; i < 4; i++) { // 🔥 fixed to 4 weeks

                boolean exists = reservationRepository.existsBySlotAndDateAndStatus(
                    slot,
                    currentDate,
                    ReservationStatus.BOOKED
                );

                if (exists) {
                    throw new RuntimeException("Slot already booked on " + currentDate);
                }

                reservations.add(buildReservation(player, stadium, slot, currentDate, subscriptionId,BookingType.APP,LocalDateTime.now(), request.getType()));

                currentDate = currentDate.plusWeeks(1);
            }
        }
        profile.setReservationCount(profile.getReservationCount() + 1);

        return reservationRepository.saveAll(reservations)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }
    // ================================
    // Cancel a reservation by slot and date (user cancel)
    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found."));

        User currentUser = JwtUtil.getCurrentUser();

        // Verify reservation ownership
        if (!reservation.getPlayer().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException(
                "You are not authorized to cancel this reservation."
            );
        }

        // Only booked reservations can be cancelled
        if (reservation.getStatus() != ReservationStatus.BOOKED) {
            throw new IllegalStateException(
                "Only booked reservations can be cancelled."
            );
        }

        // Check the 24-hour cancellation policy
        LocalDateTime reservationStart = LocalDateTime.of(
            reservation.getDate(),
            reservation.getStartTime()
        );

        if (reservationStart.isBefore(LocalDateTime.now().plusHours(24))
            || reservationStart.isEqual(LocalDateTime.now().plusHours(24))) {
            throw new IllegalStateException(
                "Reservations can only be cancelled more than 24 hours before their start time."
            );
        }

        // Apply reliability penalty
        playerReliabilityService.cancelReservation(reservation);

        // Update reservation status
        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToResponse(savedReservation);
    }

    // ================================
    // List all reservations for a player
    @Override
    public List<ReservationResponse> listReservationsForPlayer() {
        User player = JwtUtil.getCurrentUser();
        return reservationRepository.findByPlayerUserId(player.getUserId())
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ================================
    // List all reservations for a stadium (admin)
    @Override
    public List<ReservationResponse> listReservationsForStadium(Long stadiumId) {
        return reservationRepository.findByStadiumId(stadiumId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ================================
    // Get a reservation by ID
    @Override
    public ReservationResponse getReservationById(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found"));

        return mapToResponse(reservation);
    }

    @Override
    public List<ReservationResponse> getReservationsByDateRange(
        LocalDate start,
        LocalDate end
    ) {

        Long ownerId = JwtUtil.getCurrentUser().getUserId();

        LocalDate today = LocalDate.now();

        if (start == null) start = today.with(DayOfWeek.MONDAY);
        if (end == null) end = start.plusDays(6);

        // ❗ IMPORTANT: DO NOT pre-filter by date in DB for subscriptions
        List<Reservation> reservations =
            reservationRepository.findAllByOwner(ownerId);

        List<Reservation> result = new ArrayList<>();

        // 1. ONE GAME → filter by range normally
        for (Reservation r : reservations) {
            if (r.getReservationType() == ReservationType.ONE_GAME) {
                if (!r.getDate().isBefore(start) && !r.getDate().isAfter(end)) {
                    result.add(r);
                }
            }
        }

        // 2. SUBSCRIPTION → group ALL first
        Map<String, List<Reservation>> grouped = new HashMap<>();

        for (Reservation r : reservations) {
            if (r.getReservationType() == ReservationType.SUBSCRIPTION
                && r.getSubscriptionId() != null) {

                grouped
                    .computeIfAbsent(r.getSubscriptionId(), k -> new ArrayList<>())
                    .add(r);
            }
        }

        // 3. take ONLY FIRST occurrence globally
        for (List<Reservation> group : grouped.values()) {

            Reservation first = group.stream()
                .min(Comparator.comparing(Reservation::getDate))
                .orElse(null);

            if (first == null) continue;

            // show ONLY if first is inside selected range
            if (!first.getDate().isBefore(start) && !first.getDate().isAfter(end)) {
                result.add(first);
            }
        }

        return result.stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    public List<ReservationResponse> myUpcomingReservation() {
        User player = JwtUtil.getCurrentUser();

        List<Reservation> reservations =
            reservationRepository.findByPlayerAndDateGreaterThanEqualOrderByDateAsc(
                player,
                LocalDate.now()
            );

        return reservations.stream()
            .limit(5) // 👈 only few for home page (change if you want)
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    @Transactional
    public ReservationResponse completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found."));

        User currentUser = JwtUtil.getCurrentUser();

        // Verify that the current user owns the stadium
        if (!reservation.getStadium().getOwner().getUserId()
            .equals(currentUser.getUserId())) {
            throw new RuntimeException(
                "You are not authorized to complete this reservation."
            );
        }

        // Only booked reservations can be completed
        if (reservation.getStatus() != ReservationStatus.BOOKED) {
            throw new IllegalStateException(
                "Only booked reservations can be completed."
            );
        }

        // Verify that the reservation has ended
        LocalDateTime reservationEnd = LocalDateTime.of(
            reservation.getDate(),
            reservation.getEndTime()
        );

        if (LocalDateTime.now().isBefore(reservationEnd)) {
            throw new IllegalStateException(
                "A reservation cannot be completed before its end time."
            );
        }

        // Apply reliability reward (+2)
        playerReliabilityService.completeReservation(reservation);

        // Update reservation status
        reservation.setStatus(ReservationStatus.COMPLETED);

        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToResponse(savedReservation);
    }

    @Override
    @Transactional
    public ReservationResponse ownerCancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found."));

        User currentUser = JwtUtil.getCurrentUser();

        // Verify that the current user owns the stadium
        if (!reservation.getStadium().getOwner().getUserId()
            .equals(currentUser.getUserId())) {
            throw new RuntimeException(
                "You are not authorized to cancel this reservation."
            );
        }

        // Owner can only cancel direct bookings
        if (reservation.getReservationFrom() != BookingType.DIRECT) {
            throw new IllegalStateException(
                "Owners can only cancel direct bookings."
            );
        }

        // Only booked reservations can be cancelled
        if (reservation.getStatus() != ReservationStatus.BOOKED) {
            throw new IllegalStateException(
                "Only booked reservations can be cancelled."
            );
        }

        // Update reservation status
        reservation.setStatus(ReservationStatus.CANCELLED);

        Reservation savedReservation = reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }

    private Reservation buildReservation(User player, Stadium stadium, StadiumSlot slot, LocalDate date, String subscriptionId,BookingType reservationFrom, LocalDateTime now, ReservationType reservationType) {

        Reservation reservation = new Reservation();
        reservation.setPlayer(player);
        reservation.setStadium(stadium);
        reservation.setSlot(slot);
        reservation.setDate(date);
        reservation.setStartTime(slot.getStartTime());
        reservation.setEndTime(slot.getEndTime());
        reservation.setStatus(ReservationStatus.BOOKED);
        reservation.setSubscriptionId(subscriptionId);
        reservation.setCreatedAt(now);
        reservation.setReservationFrom(reservationFrom);
        reservation.setReservationType(reservationType);
        return reservation;
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
