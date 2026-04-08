package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.RatingRequest;
import com.project.tekwira.Dto.StadiumFilterDto;
import com.project.tekwira.Dto.StadiumRequest;
import com.project.tekwira.Dto.StadiumResponse;
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
    public ResponseEntity<StadiumResponse> createStadium(@RequestParam String name,
                                                         @RequestParam String address,
                                                         @RequestParam String openingTime,
                                                         @RequestParam String closingTime,
                                                         @RequestParam FieldType fieldType,
                                                         @RequestParam Double pricePerGame,
                                                         @RequestParam Double subscriptionPrice,
                                                         @RequestParam(required = false) String description,
                                                         @RequestParam(required = false) List<MultipartFile> imageFiles,
                                                         @RequestParam(required = false) MultipartFile logo) throws IOException {
        StadiumRequest request = StadiumRequest.builder()
                .name(name)
                .address(address)
                .openingTime(LocalTime.parse(openingTime))
                .closingTime(LocalTime.parse(closingTime))
                .fieldType(fieldType)
                .pricePerGame(pricePerGame)
                .subscriptionPrice(subscriptionPrice)
                .description(description)
                .imageUrls(imageFiles) // You might need to add this field in your DTO
                .logo(logo)         // You might need to add this field in your DTO
                .build();
        StadiumResponse stadium = stadiumService.createStadium(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stadium);
    }

    // 🏟 OWNER: Update stadium
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<StadiumResponse> updateStadium(
            @PathVariable Long id,
            @RequestBody StadiumRequest request
    ) {
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
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String name
            ) {
        StadiumFilterDto filter = new StadiumFilterDto();
        filter.setAddress(address);
        filter.setName(name);
        return ResponseEntity.ok(stadiumService.listAllStadiums(filter));
    }

    @GetMapping("top-rated")
    public ResponseEntity<List<StadiumResponse>> getTopRatedStadiums(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String name
    ) {
        StadiumFilterDto filter = new StadiumFilterDto();
        filter.setAddress(address);
        filter.setName(name);
        return ResponseEntity.ok(stadiumService.getTopRatedStadiums(filter));
    }

    // 📖 OWNER: Get my stadiums
    @GetMapping("/me")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<StadiumResponse>> getMyStadiums(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String name
    ) {
        User owner = JwtUtil.getCurrentUser();
        StadiumFilterDto filter = new StadiumFilterDto();
        filter.setAddress(address);
        filter.setName(name);
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
    public ResponseEntity<?> rateStadium(
            @PathVariable Long id,
            @RequestBody RatingRequest request
    ) {
        request.setStadiumId(id);
        return ResponseEntity.ok(ratingService.rateStadium(request));
    }

    @GetMapping("active-stadiums")
    public ResponseEntity<List<StadiumResponse>> getAllActiveStadiums(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String name
    ) {
        StadiumFilterDto filter = new StadiumFilterDto();
        filter.setAddress(address);
        filter.setName(name);
        return ResponseEntity.ok(stadiumService.getAllActiveStadiums(filter));
    }
}
