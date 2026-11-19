package com.project.tekwira.Repositories;

import com.project.tekwira.Models.ReliabilityTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReliabilityTransactionRepository extends JpaRepository<ReliabilityTransaction, Long> {
    boolean existsByReservation_Id(Long reservationId);
    List<ReliabilityTransaction> findByPlayerProfile_User_UserIdOrderByCreatedAtDesc(
        Long userId
    );
}
