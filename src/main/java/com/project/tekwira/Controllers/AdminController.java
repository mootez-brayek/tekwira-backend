package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.CreateOwnerRequest;
import com.project.tekwira.Dto.OwnerRevenueDto;
import com.project.tekwira.Dto.StadiumRevenueDto;
import com.project.tekwira.Models.Role;
import com.project.tekwira.Models.User;
import com.project.tekwira.Services.AdminService;
import com.project.tekwira.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @PostMapping("/owners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createOwner(@RequestBody @Valid CreateOwnerRequest request) {
        User owner = adminService.createOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(owner);
    }


    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(users);
    }

    @GetMapping("/owners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getOwners() {
        List<User> owners = userService.getAllOwners();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(owners);
    }

    @GetMapping("/players")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getPlayers() {
        List<User> players = userService.getAllPlayers();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(players);
    }

    @GetMapping("revenue/stadiums")
    @PreAuthorize("hasRole('ADMIN')")
    public List<StadiumRevenueDto> stadiumRevenue(
        @RequestParam LocalDate start,
        @RequestParam LocalDate end
    ) {
        return adminService.getStadiumRevenue(start, end);
    }

    @GetMapping("revenue/owners")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OwnerRevenueDto> ownerRevenue(
        @RequestParam LocalDate start,
        @RequestParam LocalDate end
    ) {
        return adminService.getOwnerRevenue(start, end);
    }
}
