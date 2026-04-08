package com.project.tekwira.Repositories;

import com.project.tekwira.Dto.StadiumResponse;
import com.project.tekwira.Models.BookingType;
import com.project.tekwira.Models.Stadium;
import com.project.tekwira.Models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    List<Stadium> findByOwner(User owner);
    boolean existsByOwnerAndName(User owner, String stadiumName);

    @Query("""
        SELECT s FROM Stadium s
        LEFT JOIN Rating r ON r.stadium.id = s.id
        GROUP BY s.id
        ORDER BY AVG(r.score) DESC
        """)
    List<Stadium> findTopRatedStadiums();

    @Query("""
        SELECT s FROM Stadium s
        WHERE s.isActive = true
        """)
    List<Stadium> getAllActiveStadiums();

    @Query("SELECT COUNT(s) FROM Stadium s")
    long countAllStadiums();

    @Query("""
    SELECT r.stadium.name AS stadiumName,
           COUNT(r) AS bookings,
           SUM(CASE WHEN r.reservationFrom = :app THEN 1 ELSE 0 END) AS appBookings,
           SUM(CASE WHEN r.reservationFrom = :direct THEN 1 ELSE 0 END) AS directBookings
    FROM Reservation r
    WHERE r.date BETWEEN :start AND :end
    GROUP BY r.stadium.name
    ORDER BY bookings DESC
""")
    List<Object[]> findTopStadiumsThisWeek(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("app") BookingType app,
            @Param("direct") BookingType direct,
            Pageable pageable
    );

    int countByOwner(User owner);

}
