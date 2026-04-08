package com.project.tekwira.Controllers;

import com.project.tekwira.Dto.CreateOWnerRequest;
import com.project.tekwira.Models.User;
import com.project.tekwira.Services.AdminService;
import com.project.tekwira.Services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @PostMapping("/owners")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createOwner(@RequestBody @Valid CreateOWnerRequest request) {
        User owner = adminService.createOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(owner);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(users);
    }
}
