package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.OwnerDashboardStatsDto;
import com.project.tekwira.Models.User;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.OwnerDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/dashboard")
public class OwnerDashboardController {


    private final OwnerDashboardService ownerDashboardService;

    public OwnerDashboardController(OwnerDashboardService ownerDashboardService) {
        this.ownerDashboardService = ownerDashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<OwnerDashboardStatsDto> getDashboardStats() {
        User owner = JwtUtil.getCurrentUser();
        OwnerDashboardStatsDto stats = ownerDashboardService.getStats(owner);
        return ResponseEntity.ok(stats);
    }
}
