package com.project.tekwira.Repositories;

import com.project.tekwira.Models.BookingType;
import com.project.tekwira.Models.Reservation;
import com.project.tekwira.Models.StadiumSlot;
import com.project.tekwira.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
@Query("""
    SELECT r FROM Reservation r
    WHERE r.stadium.id = :stadiumId
    AND r.date = :date
    AND r.status <> 'CANCELLED'""")
    List<Reservation> findConflictingReservations(
            Long stadiumId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );

    Optional<Object> findByStadiumIdAndSlotIdAndDate(Long stadiumId, Long slotId, LocalDate date);

    // ✅ Get reservation by slot and date
    Optional<Reservation> findBySlotAndDate(StadiumSlot slot, LocalDate date);
    // ✅ List all reservations for a given player
    List<Reservation> findByPlayerUserId(Long playerId);

    // ✅ List all reservations for a given stadium
    List<Reservation> findByStadiumId(Long stadiumId);

    // Find reservations for a specific stadium and date
    List<Reservation> findByStadiumIdAndDate(Long stadiumId, LocalDate date);

    // Check if a slot is already booked for a given date
    boolean existsBySlotAndDate(StadiumSlot slot, LocalDate date);

    long countByDateBetween(LocalDate start, LocalDate end);

    long countByDateBetweenAndReservationFrom(
            LocalDate start,
            LocalDate end,
            BookingType reservationFrom
    );

    @Query("""
    SELECT r.player.username AS playerName, COUNT(r) AS bookings
    FROM Reservation r
    WHERE r.date BETWEEN :start AND :end
    GROUP BY r.player.username
    ORDER BY bookings DESC
""")
    List<Object[]> findTopPlayersThisWeek(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          Pageable pageable);

    Optional<Reservation> findBySlot(StadiumSlot slot);


    List<Reservation> findByStadiumOwnerAndDate(User owner, LocalDate date);

    List<Reservation> findByStadiumOwnerAndDateBetween(User owner, LocalDate start, LocalDate end);
}
