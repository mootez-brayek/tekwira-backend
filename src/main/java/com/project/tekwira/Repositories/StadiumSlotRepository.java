package com.project.tekwira.Repositories;

import com.project.tekwira.Models.StadiumSlot;
import com.project.tekwira.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StadiumSlotRepository extends JpaRepository<StadiumSlot, Long> {

    List<StadiumSlot> findByStadiumId(Long stadiumId);
}
