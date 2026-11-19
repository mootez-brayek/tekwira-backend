package com.project.tekwira.Services;

import com.project.tekwira.Dto.StadiumFilterDto;
import com.project.tekwira.Dto.StadiumRequest;
import com.project.tekwira.Dto.StadiumResponse;
import com.project.tekwira.Models.Stadium;
import com.project.tekwira.Models.User;

import java.io.IOException;
import java.util.List;

public interface StadiumService {

    StadiumResponse createStadium(StadiumRequest request) throws IOException;

    // 🏟 Owner updates their stadium
    StadiumResponse updateStadium(Long stadiumId, StadiumRequest request)throws IOException;

    // 🏟 Owner deletes their stadium
    void deleteStadium(Long stadiumId);

    // 📖 List all stadiums (for players)
    List<StadiumResponse> listAllStadiums(StadiumFilterDto filter);

    // 📖 List stadiums by owner
    List<StadiumResponse> listStadiumsByOwner(User owner, StadiumFilterDto filter);

    // 📖 Get a stadium by id
    StadiumResponse getStadiumById(Long stadiumId);

    void toggleActive(Long stadiumId);

    List<StadiumResponse> getTopRatedStadiums(StadiumFilterDto filter);

    List<StadiumResponse> getAllActiveStadiums(StadiumFilterDto filter);
}
