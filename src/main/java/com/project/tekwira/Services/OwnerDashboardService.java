package com.project.tekwira.Services;

import com.project.tekwira.Dto.OwnerDashboardStatsDto;
import com.project.tekwira.Models.User;

import java.time.LocalDate;

public interface OwnerDashboardService {

    OwnerDashboardStatsDto getStats(User owner, LocalDate startDate, LocalDate endDate);
}
