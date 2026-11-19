package com.project.tekwira.Repositories;

import com.project.tekwira.Models.NoShowReport;
import com.project.tekwira.Models.NoShowReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoShowReportRepository extends JpaRepository<NoShowReport, Long> {

    boolean existsByReservation_Id(Long reservationId);

    List<NoShowReport> findByStatus(NoShowReportStatus status);
}
