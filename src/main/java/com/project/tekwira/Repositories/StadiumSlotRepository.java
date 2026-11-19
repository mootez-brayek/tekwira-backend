package com.project.tekwira.Repositories;

import com.project.tekwira.Models.StadiumSlot;
import com.project.tekwira.Models.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StadiumSlotRepository extends JpaRepository<StadiumSlot, Long> {

    List<StadiumSlot> findByStadiumId(Long stadiumId);

    @Modifying
    @Query("DELETE FROM StadiumSlot s WHERE s.stadium.id = :stadiumId")
    int deleteStadiumSlotByStadiumId(@Param("stadiumId") Long stadiumId);

    @Query(
        value = "SELECT * FROM stadium_slot WHERE id = :slotId FOR UPDATE",
        nativeQuery = true
    )
    Optional<StadiumSlot> findByIdWithLock(@Param("slotId") Long slotId);
}
