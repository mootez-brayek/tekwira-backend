package com.project.tekwira.Services;

import com.project.tekwira.Dto.OwnerDashboardStatsDto;
import com.project.tekwira.Models.User;

public interface OwnerDashboardService {

    OwnerDashboardStatsDto getStats(User owner);
}
