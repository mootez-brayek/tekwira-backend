package com.project.tekwira.Services;

import com.project.tekwira.Dto.RatingRequest;
import com.project.tekwira.Models.Rating;

public interface RatingService {

    Rating rateStadium(RatingRequest request);
}
