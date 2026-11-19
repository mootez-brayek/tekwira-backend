package com.project.tekwira.Services;

import com.project.tekwira.Dto.NoShowReportResponse;
import com.project.tekwira.Models.NoShowReport;

import java.util.List;

public interface NoShowReportService {

    // Stadium owner reports a player who didn't attend
    NoShowReportResponse reportNoShow(Long reservationId, String reason);

    // Player disputes a no-show report
    NoShowReportResponse disputeNoShow(Long reportId, String reason);

    // Admin confirms the no-show (-25 reliability points)
    NoShowReportResponse confirmNoShow(Long reportId);

    // Admin rejects the report (no reliability penalty)
    NoShowReportResponse rejectNoShow(Long reportId);

    // Admin retrieves all pending reports
    List<NoShowReportResponse> getPendingReports();
}
