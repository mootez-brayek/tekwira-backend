package com.project.tekwira.Services.implementations;

import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.ReservationType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RevenueCalculator {

    public double calculateReservationRevenue(
        Reservation reservation,
        Set<String> countedSubscriptions
    ) {

        int totalPlayers = reservation.getStadium()
            .getFieldType()
            .getTotalPlayers();

        // ONE GAME
        if (reservation.getReservationType() == ReservationType.ONE_GAME) {
            return reservation.getStadium().getPricePerGame() * totalPlayers;
        }

        // SUBSCRIPTION (deduplicated)
        if (reservation.getReservationType() == ReservationType.SUBSCRIPTION) {

            String subId = reservation.getSubscriptionId();

            if (subId == null || countedSubscriptions.contains(subId)) {
                return 0;
            }

            countedSubscriptions.add(subId);

            return reservation.getStadium()
                .getSubscriptionPrice() * totalPlayers;
        }

        return 0;
    }
}
