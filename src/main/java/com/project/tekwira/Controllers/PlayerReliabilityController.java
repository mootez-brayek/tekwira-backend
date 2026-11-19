package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.PlayerReliabilityResponse;
import com.project.tekwira.Dto.ReliabilityTransactionResponse;
import com.project.tekwira.Services.PlayerReliabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/player/reliability")
@PreAuthorize("hasRole('PLAYER')")
public class PlayerReliabilityController {
    private final PlayerReliabilityService playerReliabilityService;

    public PlayerReliabilityController(
        PlayerReliabilityService playerReliabilityService) {
        this.playerReliabilityService = playerReliabilityService;
    }

    // Get current player's reliability score and statistics
    @GetMapping
    public ResponseEntity<PlayerReliabilityResponse> getMyReliability() {

        return ResponseEntity.ok(
            playerReliabilityService.getMyReliability()
        );
    }

    // Get current player's reliability history
    @GetMapping("/history")
    public ResponseEntity<List<ReliabilityTransactionResponse>>
    getMyReliabilityHistory() {

        return ResponseEntity.ok(
            playerReliabilityService.getMyReliabilityHistory()
        );
    }
}
