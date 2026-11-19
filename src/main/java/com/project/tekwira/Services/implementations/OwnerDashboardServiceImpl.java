package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.OwnerDashboardStatsDto;
import com.project.tekwira.Models.BookingType;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.ReservationType;
import com.project.tekwira.Models.User;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Services.OwnerDashboardService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
public class OwnerDashboardServiceImpl  implements OwnerDashboardService {
    private final StadiumRepository stadiumRepository;
    private final ReservationRepository reservationRepository;
    private final RevenueCalculator revenueCalculator;

    public OwnerDashboardServiceImpl(StadiumRepository stadiumRepository, ReservationRepository reservationRepository, RevenueCalculator revenueCalculator) {
        this.stadiumRepository = stadiumRepository;
        this.reservationRepository = reservationRepository;
        this.revenueCalculator = revenueCalculator;
    }

    @Override
    public OwnerDashboardStatsDto getStats(
        User owner,
        LocalDate startDate,
        LocalDate endDate
    ) {

        if (startDate == null) {
            startDate = LocalDate.now().with(DayOfWeek.MONDAY);
        }

        if (endDate == null) {
            endDate = startDate.plusDays(6);
        }

        List<Reservation> reservations =
            reservationRepository.findByStadiumOwnerAndDateBetween(
                owner,
                startDate,
                endDate
            );

        // =========================
        // FIND FIRST SUBSCRIPTION DATE (GLOBAL PER OWNER CONTEXT)
        // =========================
        Map<String, LocalDate> subscriptionFirstDate = new HashMap<>();

        for (Reservation r : reservationRepository.findAllByOwner(owner.getUserId())) {

            if (r.getReservationType() == ReservationType.SUBSCRIPTION
                && r.getSubscriptionId() != null) {

                subscriptionFirstDate.merge(
                    r.getSubscriptionId(),
                    r.getDate(),
                    (oldDate, newDate) ->
                        oldDate.isBefore(newDate) ? oldDate : newDate
                );
            }
        }

        Set<String> countedSubscriptions = new HashSet<>();

        int totalBookings = 0;
        int appReservations = 0;
        int directReservations = 0;
        double revenue = 0;

        for (Reservation r : reservations) {

            // =========================
            // ONE GAME → always counted normally
            // =========================
            if (r.getReservationType() == ReservationType.ONE_GAME) {

                revenue += revenueCalculator.calculateReservationRevenue(
                    r,
                    countedSubscriptions
                );

                totalBookings++;

                if (r.getReservationFrom() == BookingType.APP) {
                    appReservations++;
                }

                if (r.getReservationFrom() == BookingType.DIRECT) {
                    directReservations++;
                }
            }

            // =========================
            // SUBSCRIPTION → ONLY IF FIRST DATE IN RANGE
            // =========================
            else if (r.getReservationType() == ReservationType.SUBSCRIPTION) {

                String subId = r.getSubscriptionId();
                if (subId == null) continue;

                LocalDate firstDate = subscriptionFirstDate.get(subId);

                boolean isInRange =
                    firstDate != null &&
                        !firstDate.isBefore(startDate) &&
                        !firstDate.isAfter(endDate);

                boolean notAlreadyCounted =
                    !countedSubscriptions.contains(subId);

                if (isInRange && notAlreadyCounted) {

                    // 🚨 IMPORTANT: only add revenue ONCE per subscription
                    revenue += r.getStadium().getSubscriptionPrice()
                        * r.getStadium().getFieldType().getTotalPlayers();

                    countedSubscriptions.add(subId);

                    totalBookings++;

                    if (r.getReservationFrom() == BookingType.APP) {
                        appReservations++;
                    }

                    if (r.getReservationFrom() == BookingType.DIRECT) {
                        directReservations++;
                    }
                }
            }
        }

        int totalStadiums = stadiumRepository.countByOwner(owner);

        return new OwnerDashboardStatsDto(
            totalStadiums,
            totalBookings,
            revenue,
            appReservations,
            directReservations
        );
    }
}
