package com.project.tekwira.Services.implementations;


import com.project.tekwira.Dto.NoShowReportResponse;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.NoShowReportRepository;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.NoShowReportService;
import com.project.tekwira.Services.PlayerReliabilityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoShowReportServiceImpl implements NoShowReportService {
    private final NoShowReportRepository noShowReportRepository;
    private final ReservationRepository reservationRepository;
    private final PlayerReliabilityService playerReliabilityService;

    public NoShowReportServiceImpl(
        NoShowReportRepository noShowReportRepository,
        ReservationRepository reservationRepository,
        PlayerReliabilityService playerReliabilityService) {

        this.noShowReportRepository = noShowReportRepository;
        this.reservationRepository = reservationRepository;
        this.playerReliabilityService = playerReliabilityService;
    }

    @Override
    public NoShowReportResponse reportNoShow(Long reservationId, String reason) {
        User currentUser = JwtUtil.getCurrentUser();

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() ->
                new IllegalStateException("Reservation not found."));

        // Verify stadium ownership
        if (!reservation.getStadium().getOwner().getUserId()
            .equals(currentUser.getUserId())) {
            throw new IllegalStateException(
                "You are not authorized to report this reservation."
            );
        }

        // Only booked reservations can be reported
        if (reservation.getStatus() != ReservationStatus.BOOKED) {
            throw new IllegalStateException(
                "Only booked reservations can be reported."
            );
        }

        // The reservation must have ended
        LocalDateTime reservationEnd = LocalDateTime.of(
            reservation.getDate(),
            reservation.getEndTime()
        );

        if (LocalDateTime.now().isBefore(reservationEnd)) {
            throw new IllegalStateException(
                "You cannot report a no-show before the reservation ends."
            );
        }

        // Prevent duplicate reports
        if (noShowReportRepository.existsByReservation_Id(reservationId)) {
            throw new IllegalStateException(
                "A no-show report already exists for this reservation."
            );
        }

        if (reason == null || reason.isBlank() || reason.length() > 500) {
            throw new IllegalArgumentException(
                "A valid reason (maximum 500 characters) is required."
            );
        }

        NoShowReport report = NoShowReport.builder()
            .reservation(reservation)
            .reportedBy(currentUser)
            .reason(reason.trim())
            .status(NoShowReportStatus.PENDING)
            .reportedAt(LocalDateTime.now())
            .build();

        NoShowReport savedReport = noShowReportRepository.save(report);

        return mapToResponse(savedReport);
    }

    @Override
    public NoShowReportResponse disputeNoShow(Long reportId, String reason) {
        User currentUser = JwtUtil.getCurrentUser();

        NoShowReport report = noShowReportRepository.findById(reportId)
            .orElseThrow(() ->
                new IllegalStateException("No-show report not found."));

        // Verify that the current user is the affected player
        if (!report.getReservation().getPlayer().getUserId()
            .equals(currentUser.getUserId())) {
            throw new IllegalStateException(
                "You are not authorized to dispute this report."
            );
        }

        if (report.getStatus() != NoShowReportStatus.PENDING) {
            throw new IllegalStateException(
                "Only pending reports can be disputed."
            );
        }

        if (reason == null || reason.isBlank() || reason.length() > 1000) {
            throw new IllegalArgumentException(
                "A valid dispute reason (maximum 1000 characters) is required."
            );
        }

        report.setPlayerDisputeReason(reason.trim());
        report.setStatus(NoShowReportStatus.DISPUTED);
        report.setDisputedAt(LocalDateTime.now());

        NoShowReport savedReport = noShowReportRepository.save(report);

        return mapToResponse(savedReport);
    }

    @Override
    public NoShowReportResponse confirmNoShow(Long reportId) {

        NoShowReport report = noShowReportRepository.findById(reportId)
            .orElseThrow(() ->
                new IllegalStateException("No-show report not found."));

        if (report.getStatus() != NoShowReportStatus.PENDING
            && report.getStatus() != NoShowReportStatus.DISPUTED) {
            throw new IllegalStateException(
                "This report has already been reviewed."
            );
        }

        Reservation reservation = report.getReservation();

        if (reservation.getStatus() != ReservationStatus.BOOKED) {
            throw new IllegalStateException(
                "This reservation is no longer eligible for a no-show decision."
            );
        }

        // Apply -25 reliability points
        playerReliabilityService.markNoShow(reservation);

        // Update reservation status
        reservation.setStatus(ReservationStatus.NO_SHOW);
        reservationRepository.save(reservation);

        // Confirm report
        report.setStatus(NoShowReportStatus.CONFIRMED);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedBy(JwtUtil.getCurrentUser());

        NoShowReport savedReport = noShowReportRepository.save(report);

        return mapToResponse(savedReport);
    }

    @Override
    public NoShowReportResponse rejectNoShow(Long reportId) {
        NoShowReport report = noShowReportRepository.findById(reportId)
            .orElseThrow(() ->
                new IllegalStateException("No-show report not found."));

        if (report.getStatus() != NoShowReportStatus.PENDING
            && report.getStatus() != NoShowReportStatus.DISPUTED) {
            throw new IllegalStateException(
                "This report has already been reviewed."
            );
        }

        report.setStatus(NoShowReportStatus.REJECTED);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedBy(JwtUtil.getCurrentUser());

        NoShowReport savedReport = noShowReportRepository.save(report);

        return mapToResponse(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoShowReportResponse> getPendingReports() {
        return noShowReportRepository.findByStatus(
            NoShowReportStatus.PENDING
        )  .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    private NoShowReportResponse mapToResponse(NoShowReport report) {

        Reservation reservation = report.getReservation();

        return NoShowReportResponse.builder()
            .id(report.getId())
            .reservationId(reservation.getId())
            .playerName(reservation.getPlayer().getUsername())
            .ownerName(report.getReportedBy().getUsername())
            .reason(report.getReason())
            .playerDisputeReason(report.getPlayerDisputeReason())
            .status(report.getStatus())
            .reportedAt(report.getReportedAt())
            .disputedAt(report.getDisputedAt())
            .reviewedAt(report.getReviewedAt())
            .build();
    }

}
