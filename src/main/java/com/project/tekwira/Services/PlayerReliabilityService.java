package com.project.tekwira.Services;

import com.project.tekwira.Dto.PlayerReliabilityResponse;
import com.project.tekwira.Dto.ReliabilityTransactionResponse;
import com.project.tekwira.Models.Reservation;

import java.util.List;

public interface PlayerReliabilityService {
    void completeReservation(Reservation reservation);

    void cancelReservation(Reservation reservation);

    void markNoShow(Reservation reservation);

    PlayerReliabilityResponse getMyReliability();

    List<ReliabilityTransactionResponse> getMyReliabilityHistory();
}
