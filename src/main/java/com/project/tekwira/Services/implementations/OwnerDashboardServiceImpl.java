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

@Service
public class OwnerDashboardServiceImpl  implements OwnerDashboardService {
    private final StadiumRepository stadiumRepository;
    private final ReservationRepository reservationRepository;

    public OwnerDashboardServiceImpl(StadiumRepository stadiumRepository, ReservationRepository reservationRepository) {
        this.stadiumRepository = stadiumRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public OwnerDashboardStatsDto getStats(User owner) {
        int totalStadiums = stadiumRepository.countByOwner(owner);

        LocalDate today = LocalDate.now();
        int totalBookingsToday = reservationRepository.findByStadiumOwnerAndDate(owner, today).size();

        // Revenue this week
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        double revenueThisWeek = reservationRepository.findByStadiumOwnerAndDateBetween(owner, startOfWeek, endOfWeek)
                .stream()
                .mapToDouble(this::calculateReservationRevenue)
                .sum();

        return new OwnerDashboardStatsDto(totalStadiums, totalBookingsToday, revenueThisWeek);
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
