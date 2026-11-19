package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.*;
import com.project.tekwira.Models.FieldType;
import com.project.tekwira.Models.User;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.RatingService;
import com.project.tekwira.Services.StadiumService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/stadiums")
@CrossOrigin("*")
public class StadiumController {

    private final StadiumService stadiumService;
    private final RatingService ratingService;

    public StadiumController(StadiumService stadiumService, RatingService ratingService) {
        this.stadiumService = stadiumService;
        this.ratingService = ratingService;
    }

    // 🏟 OWNER: Create stadium
    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<StadiumResponse> createStadium(
        @ModelAttribute StadiumRequest request
    ) throws IOException {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(stadiumService.createStadium(request));
    }

    // 🏟 OWNER: Update stadium
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<StadiumResponse> updateStadium(
        @PathVariable Long id,
        @ModelAttribute StadiumRequest request
    )throws IOException {
        StadiumResponse stadium = stadiumService.updateStadium(id, request);
        return ResponseEntity.ok(stadium);
    }
    // 🏟 OWNER: Delete stadium
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStadium(@PathVariable Long id) {
        stadiumService.deleteStadium(id);
        return ResponseEntity.noContent().build();
    }

    // 📖 PLAYER: List all stadiums
    @GetMapping
    public ResponseEntity<List<StadiumResponse>> getAllStadiums(
        @ModelAttribute StadiumFilterDto filter
            ) {
        filter.setGovernorate(filter.getGovernorate());
        filter.setName(filter.getName());
        return ResponseEntity.ok(stadiumService.listAllStadiums(filter));
    }

    @GetMapping("top-rated")
    public ResponseEntity<List<StadiumResponse>> getTopRatedStadiums(StadiumFilterDto filter) {
        return ResponseEntity.ok(stadiumService.getTopRatedStadiums(filter));
    }

    // 📖 OWNER: Get my stadiums
    @GetMapping("/me")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<StadiumResponse>> getMyStadiums(
        @ModelAttribute StadiumFilterDto filter
    ) {
        User owner = JwtUtil.getCurrentUser();
        filter.setGovernorate(filter.getGovernorate());
        filter.setName(filter.getName());
        return ResponseEntity.ok(stadiumService.listStadiumsByOwner(owner, filter));
    }

    // 📖 ANY: Get stadium by ID
    @GetMapping("/{id}")
    public ResponseEntity<StadiumResponse> getStadiumById(@PathVariable Long id) {
        return ResponseEntity.ok(stadiumService.getStadiumById(id));
    }

    @PostMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        stadiumService.toggleActive(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rate")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<RatingResponse> rateStadium(
            @PathVariable Long id,
            @RequestBody RatingRequest request
    ) {
        request.setStadiumId(id);
        return ResponseEntity.ok(ratingService.rateStadium(request));
    }

    @GetMapping("/active-stadiums")
    public ResponseEntity<List<StadiumResponse>> getAllActiveStadiums(
        @ModelAttribute StadiumFilterDto filter ) {
        return ResponseEntity.ok(stadiumService.getAllActiveStadiums(filter));
    }
}
