package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.CreateOwnerRequest;
import com.project.tekwira.Dto.OwnerRevenueDto;
import com.project.tekwira.Dto.StadiumRevenueDto;
import com.project.tekwira.Models.*;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Repositories.UserRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.AdminService;
import com.project.tekwira.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RevenueCalculator revenueCalculator;

    private final ReservationRepository reservationRepository;

    @Transactional
    @Override
    public User createOwner(CreateOwnerRequest request) {
        User principalUser = JwtUtil.getCurrentUser();

        // 2️⃣ Fetch managed entity from database
        User user = userRepository.findById(principalUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (! user.getRole().equals(Role.ADMIN)) {
            throw new RuntimeException("Only admins can create owner");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 2️⃣ Create Owner user
        User owner = new User();
        owner.setEmail(request.getEmail());
        owner.setUsername(request.getUsername());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setFirstName(request.getFirstName());
        owner.setCreatedAt(LocalDateTime.now());
        owner.setLastName(request.getLastName());
        owner.setPhoneNumber(request.getPhoneNumber());
        owner.setRole(Role.OWNER);
        User savedOwner = userRepository.save(owner);
        userService.forgotPassword(savedOwner.getEmail());
        return savedOwner;
    }

    @Override
    public List<StadiumRevenueDto> getStadiumRevenue(
        LocalDate start,
        LocalDate end
    ) {

        List<Reservation> reservations =
            reservationRepository.findActiveReservations(start, end);

        // =========================
        // STEP 1: FIND FIRST DATE OF EACH SUBSCRIPTION (GLOBAL)
        // =========================
        Map<String, LocalDate> subscriptionFirstDate = new HashMap<>();

        for (Reservation r : reservationRepository.findAll()) {

            if (r.getReservationType() == ReservationType.SUBSCRIPTION
                && r.getSubscriptionId() != null) {

                subscriptionFirstDate.merge(
                    r.getSubscriptionId(),
                    r.getDate(),
                    (oldDate, newDate) ->
                        oldDate.isBefore(newDate) ? oldDate : newDate
                );
            }
        }

        Map<Long, List<Reservation>> byStadium =
            reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getStadium().getId()));

        List<StadiumRevenueDto> result = new ArrayList<>();

        for (var entry : byStadium.entrySet()) {

            List<Reservation> stadiumReservations = entry.getValue();

            if (stadiumReservations.isEmpty()) continue;

            Set<String> countedSubscriptions = new HashSet<>();

            double revenue = 0;
            long oneGameBookings = 0;
            long subscriptionBookings = 0;

            for (Reservation r : stadiumReservations) {

                // =========================
                // ONE GAME → always included if in range
                // =========================
                if (r.getReservationType() == ReservationType.ONE_GAME) {

                    revenue += revenueCalculator.calculateReservationRevenue(
                        r,
                        countedSubscriptions
                    );

                    oneGameBookings++;
                }

                // =========================
                // SUBSCRIPTION → ONLY FIRST DATE MATTERS
                // =========================
                else if (r.getReservationType() == ReservationType.SUBSCRIPTION) {

                    String subId = r.getSubscriptionId();
                    if (subId == null) continue;

                    LocalDate firstDate = subscriptionFirstDate.get(subId);

                    // 🚨 KEY FIX:
                    // Only count if FIRST reservation is inside range
                    if (!countedSubscriptions.contains(subId)
                        && firstDate != null
                        && !firstDate.isBefore(start)
                        && !firstDate.isAfter(end)) {

                        revenue += revenueCalculator.calculateReservationRevenue(
                            r,
                            countedSubscriptions
                        );

                        countedSubscriptions.add(subId);
                        subscriptionBookings++;
                    }
                }
            }

            long totalBookings = oneGameBookings + subscriptionBookings;

            Reservation sample = stadiumReservations.get(0);

            result.add(new StadiumRevenueDto(
                sample.getStadium().getId(),
                sample.getStadium().getName(),
                sample.getStadium().getOwner().getFirstName(),
                revenue,
                totalBookings,
                oneGameBookings,
                subscriptionBookings,
                sample.getStadium().getOwner().getUserId()
            ));
        }

        return result;
    }

    @Override
    public List<OwnerRevenueDto> getOwnerRevenue(LocalDate start, LocalDate end) {
        List<StadiumRevenueDto> stadiums = getStadiumRevenue(start, end);

        Map<Long, List<StadiumRevenueDto>> byOwner =
            stadiums.stream()
                .collect(Collectors.groupingBy(StadiumRevenueDto::getOwnerId));

        List<OwnerRevenueDto> result = new ArrayList<>();

        for (var entry : byOwner.entrySet()) {

            List<StadiumRevenueDto> ownerStadiums = entry.getValue();

            double totalRevenue = ownerStadiums.stream()
                .mapToDouble(StadiumRevenueDto::getRevenue)
                .sum();

            result.add(new OwnerRevenueDto(
                entry.getKey(),
                ownerStadiums.get(0).getOwnerName(),
                totalRevenue,
                ownerStadiums.size()
            ));
        }

        return result;
    }
}
