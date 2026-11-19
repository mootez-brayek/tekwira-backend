package com.project.tekwira.Repositories;

import com.project.tekwira.Dto.RatingStats;
import com.project.tekwira.Dto.StadiumResponse;
import com.project.tekwira.Models.*;
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
        WHERE s.isActive = true
        AND (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:address IS NULL OR LOWER(s.address) LIKE LOWER(CONCAT('%', :address, '%')))
        AND (:fieldType IS NULL OR s.fieldType = :fieldType)
        GROUP BY s.id
        ORDER BY COALESCE(AVG(r.score), 0) DESC
    """)
    List<Stadium> findTopRatedStadiums(
        @Param("name") String name,
        @Param("address") String address,
        @Param("fieldType") FieldType fieldType
    );

    @Query("""
    SELECT s FROM Stadium s
    WHERE s.isActive = true
    AND (:name IS NULL OR LOWER(s.name) LIKE %:name%)
    AND (:governorate IS NULL OR s.city = :governorate)
    AND (:fieldType IS NULL OR s.fieldType = :fieldType)
    """)
    List<Stadium> getAllActiveStadiums(
        @Param("name") String name,
        @Param("governorate") Governorate governorate,
        @Param("fieldType") FieldType fieldType
    );

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
