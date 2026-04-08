package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.OwnerDashboardStatsDto;
import com.project.tekwira.Models.User;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.OwnerDashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/owner/dashboard")
public class OwnerDashboardController {


    private final OwnerDashboardService ownerDashboardService;

    public OwnerDashboardController(OwnerDashboardService ownerDashboardService) {
        this.ownerDashboardService = ownerDashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<OwnerDashboardStatsDto> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User currentUser
    ) {
        OwnerDashboardStatsDto stats = ownerDashboardService.getStats(currentUser, startDate, endDate);
        return ResponseEntity.ok(stats);
    }
}
