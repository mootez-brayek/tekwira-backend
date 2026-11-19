package com.project.tekwira.Repositories;

import com.project.tekwira.Models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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
    WHERE r.date BETWEEN :start AND :end AND r.player.role = com.project.tekwira.Models.Role.PLAYER
    GROUP BY r.player.username
    ORDER BY bookings DESC
""")
    List<Object[]> findTopPlayersThisWeek(@Param("start") LocalDate start,
                                          @Param("end") LocalDate end,
                                          Pageable pageable);

    Optional<Reservation> findBySlot(StadiumSlot slot);

    List<Reservation> findByStadiumOwnerAndDate(User owner, LocalDate date);


    List<Reservation> findByStadiumOwnerUserIdAndDateBetweenOrderByDateAscStartTimeAsc(Long ownerId, LocalDate start, LocalDate end);

    List<Reservation> findByPlayerAndDateGreaterThanEqualOrderByDateAsc(
        User player,
        LocalDate date
    );

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.stadium.id = :stadiumId
        AND (
            r.date > :today
            OR (r.date = :today AND r.startTime > :now)
        )
    """)
    List<Reservation> findFutureReservationsByStadiumId(
        @Param("stadiumId") Long stadiumId,
        @Param("today") LocalDate today,
        @Param("now") LocalTime now
    );

    List<Reservation> findByStadiumOwnerAndDateBetween(User owner, LocalDate start, LocalDate end);


    @Query("""
        SELECT r FROM Reservation r
        WHERE r.date BETWEEN :start AND :end
        AND r.status <> com.project.tekwira.Models.ReservationStatus.CANCELLED
        """)
    List<Reservation> findActiveReservations(LocalDate start, LocalDate end);

    @Query("""
    SELECT r
    FROM Reservation r
    WHERE r.stadium.owner.userId = :ownerId
    ORDER BY r.date ASC, r.startTime ASC
""")
    List<Reservation> findAllByOwner(@Param("ownerId") Long ownerId);

    Optional<Reservation> findFirstBySubscriptionIdOrderByDateAscStartTimeAsc(
        String subscriptionId
    );

    Optional<Reservation> findBySlotAndDateAndStatus(
        StadiumSlot slot,
        LocalDate date,
        ReservationStatus status
    );

    boolean existsBySlotAndDateAndStatus(
        StadiumSlot slot,
        LocalDate date,
        ReservationStatus status
    );
}


