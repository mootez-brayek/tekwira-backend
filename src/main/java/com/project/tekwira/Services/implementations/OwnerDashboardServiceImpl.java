package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.OwnerDashboardStatsDto;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.User;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Services.OwnerDashboardService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class OwnerDashboardServiceImpl  implements OwnerDashboardService {
    private final StadiumRepository stadiumRepository;
    private final ReservationRepository reservationRepository;

    public OwnerDashboardServiceImpl(StadiumRepository stadiumRepository, ReservationRepository reservationRepository) {
        this.stadiumRepository = stadiumRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public OwnerDashboardStatsDto getStats(User owner, LocalDate startDate, LocalDate endDate) {
        int totalStadiums = stadiumRepository.countByOwner(owner);

        LocalDate today = LocalDate.now();

        // Default to current week if start/end not provided
        if (startDate == null) {
            startDate = today.with(DayOfWeek.MONDAY);
        }
        if (endDate == null) {
            endDate = startDate.plusDays(6);
        }

        // Total bookings today (only for today, not affected by custom range)
        List<Reservation> bookingsToday = reservationRepository.findByStadiumOwnerAndDate(owner, today);
        int totalBookingsToday = bookingsToday.size();

        // Count by reservation type
        int appReservationsToday = (int) bookingsToday.stream()
                .filter(r -> "APP".equalsIgnoreCase(String.valueOf(r.getReservationFrom())))
                .count();

        int directReservationsToday = (int) bookingsToday.stream()
                .filter(r -> "DIRECT".equalsIgnoreCase(String.valueOf(r.getReservationFrom())))
                .count();

        // Revenue for the selected range
        double revenue = reservationRepository.findByStadiumOwnerAndDateBetween(owner, startDate, endDate)
                .stream()
                .mapToDouble(this::calculateReservationRevenue)
                .sum();

        return new OwnerDashboardStatsDto(
                totalStadiums,
                totalBookingsToday,
                revenue,
                appReservationsToday,
                directReservationsToday
        );
    }
    private double calculateReservationRevenue(Reservation reservation) {
        int totalPlayers = reservation.getSlot().getStadium().getFieldType().getTotalPlayers();
        double pricePerPlayer;

        if (reservation.getSubscriptionId() != null && !reservation.getSubscriptionId().isEmpty()) {
            pricePerPlayer = reservation.getStadium().getSubscriptionPrice();
        } else {
            pricePerPlayer = reservation.getStadium().getPricePerGame();
        }

        return pricePerPlayer * totalPlayers;
    }
}
