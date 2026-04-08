package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.ReservationRequest;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Repositories.StadiumSlotRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.ReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final StadiumRepository stadiumRepository;
    private final ReservationRepository reservationRepository;
    private final StadiumSlotRepository stadiumSlotRepository;

    public ReservationServiceImpl(StadiumRepository stadiumRepository, ReservationRepository reservationRepository, StadiumSlotRepository stadiumSlotRepository) {
        this.stadiumRepository = stadiumRepository;
        this.reservationRepository = reservationRepository;
        this.stadiumSlotRepository = stadiumSlotRepository;
    }

    @Override
    @Transactional
    public List<Reservation> createReservation(ReservationRequest request) {

        User player = JwtUtil.getCurrentUser();
        System.out.println("TYPE = " + request.getType());
        Stadium stadium = stadiumRepository.findById(request.getStadiumId())
                .orElseThrow(() -> new RuntimeException("Stadium not found"));

        StadiumSlot slot = stadiumSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.getStadium().getId().equals(stadium.getId())) {
            throw new RuntimeException("Slot does not belong to this stadium");
        }

        List<Reservation> reservations = new ArrayList<>();

        if (request.getType() == ReservationType.ONE_GAME) {

            if (reservationRepository.existsBySlotAndDate(slot, request.getDate())) {
                throw new RuntimeException("Slot already booked");
            }

            reservations.add(buildReservation(player, stadium, slot, request.getDate(), null,BookingType.APP, LocalDateTime.now()));

        } else if (request.getType() == ReservationType.SUBSCRIPTION) {

            String subscriptionId = UUID.randomUUID().toString();
            LocalDate currentDate = request.getDate();

            for (int i = 0; i < 4; i++) { // 🔥 fixed to 4 weeks

                if (reservationRepository.existsBySlotAndDate(slot, currentDate)) {
                    throw new RuntimeException("Slot already booked on " + currentDate);
                }

                reservations.add(buildReservation(player, stadium, slot, currentDate, subscriptionId,BookingType.APP,LocalDateTime.now()));

                currentDate = currentDate.plusWeeks(1);
            }
        }

        return reservationRepository.saveAll(reservations);
    }
    // ================================
    // Cancel a reservation by slot and date (user cancel)
    @Override
    @Transactional
    public void cancelReservation(Long slotId, LocalDate date) {
        StadiumSlot slot = stadiumSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        Reservation reservation = reservationRepository.findBySlotAndDate(slot, date)
                .orElseThrow(() -> new RuntimeException("Reservation not found for this slot on the selected date"));

        // Optional: you can check if current user is the player
        User player = JwtUtil.getCurrentUser();
        if (!reservation.getPlayer().getUserId().equals(player.getUserId())) {
            throw new RuntimeException("You can only cancel your own reservations");
        }

        reservationRepository.delete(reservation);
    }

    // ================================
    // List all reservations for a player
    @Override
    public List<Reservation> listReservationsForPlayer() {
        User player = JwtUtil.getCurrentUser();
        return reservationRepository.findByPlayerUserId(player.getUserId());
    }

    // ================================
    // List all reservations for a stadium (admin)
    @Override
    public List<Reservation> listReservationsForStadium(Long stadiumId) {
        return reservationRepository.findByStadiumId(stadiumId);
    }

    // ================================
    // Get a reservation by ID
    @Override
    public Reservation getReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
    }

    private Reservation buildReservation(User player, Stadium stadium, StadiumSlot slot, LocalDate date, String subscriptionId,BookingType reservationFrom, LocalDateTime now) {

        Reservation reservation = new Reservation();
        reservation.setPlayer(player);
        reservation.setStadium(stadium);
        reservation.setSlot(slot);
        reservation.setDate(date);
        reservation.setStartTime(slot.getStartTime());
        reservation.setEndTime(slot.getEndTime());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setSubscriptionId(subscriptionId);
        reservation.setCreatedAt(now);
        reservation.setReservationFrom(reservationFrom);
        return reservation;
    }
}
