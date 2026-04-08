package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.DashboardStatsDto;
import com.project.tekwira.Dto.TopPlayerDto;
import com.project.tekwira.Dto.TopStadiumDto;
import com.project.tekwira.Models.BookingType;
import com.project.tekwira.Models.Role;
import com.project.tekwira.Repositories.ReservationRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Repositories.UserRepository;
import com.project.tekwira.Services.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final UserRepository userRepository;
    private final StadiumRepository stadiumRepository;
    private final ReservationRepository reservationRepository;

    public DashboardServiceImpl(UserRepository userRepository, StadiumRepository stadiumRepository, ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.stadiumRepository = stadiumRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public DashboardStatsDto getStats() {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);

        long totalPlayers = userRepository.countByRole(Role.PLAYER);
        long totalStadiums = stadiumRepository.countAllStadiums();
        long reservationsThisWeek = reservationRepository.countByDateBetween(startOfWeek, endOfWeek);
        long appReservations = reservationRepository.countByDateBetweenAndReservationFrom(startOfWeek, endOfWeek, BookingType.APP);
        long directReservations = reservationRepository.countByDateBetweenAndReservationFrom(startOfWeek, endOfWeek, BookingType.DIRECT);

        // Top stadiums
        List<Object[]> topStadiumRaw = stadiumRepository.findTopStadiumsThisWeek(
                startOfWeek,
                endOfWeek,
                BookingType.APP,
                BookingType.DIRECT,
                PageRequest.of(0, 3)
        );
        List<TopStadiumDto> topStadiums = topStadiumRaw.stream()
                .map(r -> new TopStadiumDto(
                        (String) r[0], // stadiumName
                        r[1] != null ? ((Number) r[1]).longValue() : 0L, // bookings
                        r[2] != null ? ((Number) r[2]).longValue() : 0L, // appBookings
                        r[3] != null ? ((Number) r[3]).longValue() : 0L  // directBookings
                ))
                .toList();

        // Top players
        List<Object[]> topPlayersRaw = reservationRepository.findTopPlayersThisWeek(startOfWeek,
                endOfWeek,
                PageRequest.of(0, 5));

        List<TopPlayerDto> topPlayers = topPlayersRaw.stream()
                .map(r -> new TopPlayerDto(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();

        return new DashboardStatsDto(
                totalPlayers,
                totalStadiums,
                reservationsThisWeek,
                appReservations,
                directReservations,
                topStadiums,
                topPlayers
        );
    }
}
