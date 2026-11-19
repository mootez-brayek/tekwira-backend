package com.project.tekwira.Services;

import com.project.tekwira.Dto.CreateOwnerRequest;
import com.project.tekwira.Dto.OwnerRevenueDto;
import com.project.tekwira.Dto.StadiumRevenueDto;
import com.project.tekwira.Models.User;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {

    User createOwner(CreateOwnerRequest request);
    List<StadiumRevenueDto> getStadiumRevenue(LocalDate start, LocalDate end);
    List<OwnerRevenueDto> getOwnerRevenue(LocalDate start, LocalDate end);
}
