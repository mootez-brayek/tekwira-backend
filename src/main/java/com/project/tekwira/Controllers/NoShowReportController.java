package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.NoShowReportResponse;
import com.project.tekwira.Models.NoShowReport;
import com.project.tekwira.Services.NoShowReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/no-show-reports")
public class NoShowReportController {
    private final NoShowReportService noShowReportService;

    public NoShowReportController(NoShowReportService noShowReportService) {
        this.noShowReportService = noShowReportService;
    }

    // OWNER: Report a player who didn't attend
    @PostMapping("/reservation/{reservationId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<NoShowReportResponse> reportNoShow(
        @PathVariable Long reservationId,
        @RequestParam String reason) {

        return ResponseEntity.ok(
            noShowReportService.reportNoShow(reservationId, reason)
        );
    }
    // PLAYER: Dispute a no-show report
    @PutMapping("/{reportId}/dispute")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<NoShowReportResponse> disputeNoShow(
        @PathVariable Long reportId,
        @RequestParam String reason) {

        return ResponseEntity.ok(
            noShowReportService.disputeNoShow(reportId, reason)
        );
    }

    // ADMIN: Confirm a no-show report (-25 points)
    @PutMapping("/{reportId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoShowReportResponse> confirmNoShow(
        @PathVariable Long reportId) {

        return ResponseEntity.ok(
            noShowReportService.confirmNoShow(reportId)
        );
    }

    // ADMIN: Reject a no-show report
    @PutMapping("/{reportId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NoShowReportResponse> rejectNoShow(
        @PathVariable Long reportId) {

        return ResponseEntity.ok(
            noShowReportService.rejectNoShow(reportId)
        );
    }

    // ADMIN: Get pending reports
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NoShowReportResponse>> getPendingReports() {

        return ResponseEntity.ok(
            noShowReportService.getPendingReports()
        );
    }
}
