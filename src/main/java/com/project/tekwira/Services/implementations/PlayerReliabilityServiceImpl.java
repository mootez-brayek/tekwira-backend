package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.PlayerReliabilityResponse;
import com.project.tekwira.Dto.ReliabilityTransactionResponse;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.PlayerProfileRepository;
import com.project.tekwira.Repositories.ReliabilityTransactionRepository;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.PlayerReliabilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerReliabilityServiceImpl implements PlayerReliabilityService {

    private final PlayerProfileRepository playerProfileRepository;
    private final ReliabilityTransactionRepository reliabilityTransactionRepository;
    private final ReservationRepository reservationRepository;

    public PlayerReliabilityServiceImpl(
        PlayerProfileRepository playerProfileRepository,
        ReliabilityTransactionRepository reliabilityTransactionRepository,
        ReservationRepository reservationRepository) {
        this.playerProfileRepository = playerProfileRepository;
        this.reliabilityTransactionRepository = reliabilityTransactionRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void completeReservation(Reservation reservation) {
        if (!shouldApplySubscriptionPenalty(reservation)) {
            return;
        }

        int points = reservation.getReservationType() == ReservationType.SUBSCRIPTION
            ? 8
            : 2;

        applyReliabilityEvent(
            reservation,
            ReliabilityEventType.RESERVATION_COMPLETED,
            points
        );
    }

    @Override
    public void cancelReservation(Reservation reservation) {
        if (!shouldApplySubscriptionPenalty(reservation)) {
            return;
        }

        int penalty = reservation.getSubscriptionId() == null ? -5 : -10;

        applyReliabilityEvent(
            reservation,
            ReliabilityEventType.RESERVATION_CANCELLED,
            penalty
        );
    }

    @Override
    public void markNoShow(Reservation reservation) {
        if (reservation == null || reservation.getDate() == null
            || reservation.getEndTime() == null) {
            throw new IllegalArgumentException("Invalid reservation.");
        }

        LocalDateTime reservationEnd = LocalDateTime.of(
            reservation.getDate(),
            reservation.getEndTime()
        );

        if (LocalDateTime.now().isBefore(reservationEnd)) {
            throw new IllegalStateException(
                "A no-show cannot be recorded before the reservation ends."
            );
        }

        if (!shouldApplySubscriptionPenalty(reservation)) {
            return;
        }

        int penalty = reservation.getSubscriptionId() == null ? -25 : -50;

        applyReliabilityEvent(
            reservation,
            ReliabilityEventType.RESERVATION_NO_SHOW,
            penalty
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerReliabilityResponse getMyReliability() {
        User currentUser = JwtUtil.getCurrentUser();

        PlayerProfile profile = playerProfileRepository
            .findByUser(currentUser)
            .orElseThrow(() -> new IllegalStateException(
                "Player profile not found."
            ));

        return PlayerReliabilityResponse.builder()
            .reliabilityScore(profile.getReliabilityScore())
            .completedReservations(profile.getCompletedReservations())
            .cancelledReservations(profile.getCancelledReservations())
            .noShowReservations(profile.getNoShowReservations())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReliabilityTransactionResponse> getMyReliabilityHistory() {
        User currentUser = JwtUtil.getCurrentUser();

        // Retrieve the current player's reliability transactions
        List<ReliabilityTransaction> transactions =
            reliabilityTransactionRepository
                .findByPlayerProfile_User_UserIdOrderByCreatedAtDesc(
                    currentUser.getUserId()
                );

        return transactions.stream()
            .map(transaction -> ReliabilityTransactionResponse.builder()
                .id(transaction.getId())
                .reservationId(transaction.getReservation().getId())
                .eventType(transaction.getEventType())
                .pointsChange(transaction.getPointsChange())
                .previousScore(transaction.getPreviousScore())
                .newScore(transaction.getNewScore())
                .createdAt(transaction.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }

    private void applyReliabilityEvent(
        Reservation reservation,
        ReliabilityEventType eventType,
        int points
    ) {
        if (reservation == null || reservation.getId() == null
            || reservation.getPlayer() == null) {
            throw new IllegalArgumentException("Invalid reservation.");
        }

        if (reliabilityTransactionRepository
            .existsByReservation_Id(reservation.getId())) {
            throw new IllegalStateException(
                "A reliability event has already been recorded for this reservation."
            );
        }

        PlayerProfile profile = playerProfileRepository
            .findByUser(reservation.getPlayer())
            .orElseThrow(() -> new IllegalStateException(
                "Player profile not found."
            ));

        int previousScore = profile.getReliabilityScore();

        int newScore = Math.max(0, Math.min(100, previousScore + points));

        int actualPointsChange = newScore - previousScore;

        profile.setReliabilityScore(newScore);

        switch (eventType) {
            case RESERVATION_COMPLETED ->
                profile.setCompletedReservations(
                    profile.getCompletedReservations() + 1
                );

            case RESERVATION_CANCELLED ->
                profile.setCancelledReservations(
                    profile.getCancelledReservations() + 1
                );

            case RESERVATION_NO_SHOW ->
                profile.setNoShowReservations(
                    profile.getNoShowReservations() + 1
                );
        }

        playerProfileRepository.save(profile);

        ReliabilityTransaction transaction = ReliabilityTransaction.builder()
            .playerProfile(profile)
            .reservation(reservation)
            .eventType(eventType)
            .pointsChange(actualPointsChange)
            .previousScore(previousScore)
            .newScore(newScore)
            .createdAt(LocalDateTime.now())
            .build();

        reliabilityTransactionRepository.save(transaction);
    }

    private boolean shouldApplySubscriptionPenalty(Reservation reservation) {

        if (reservation == null || reservation.getId() == null) {
            throw new IllegalArgumentException("Invalid reservation.");
        }

        // Individual reservation: always apply the normal penalty.
        if (reservation.getReservationType() == ReservationType.ONE_GAME) {
            return true;
        }

        if (reservation.getReservationType() != ReservationType.SUBSCRIPTION
            || reservation.getSubscriptionId() == null) {
            throw new IllegalStateException("Invalid reservation type or subscription ID.");
        }

        // Subscription: only the first session receives a penalty.
        Reservation firstSession = reservationRepository
            .findFirstBySubscriptionIdOrderByDateAscStartTimeAsc(
                reservation.getSubscriptionId()
            )
            .orElseThrow(() -> new IllegalStateException(
                "Subscription reservation not found."
            ));

        return firstSession.getId().equals(reservation.getId());
    }
}
