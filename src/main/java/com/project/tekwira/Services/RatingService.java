package com.project.tekwira.Services;

import com.project.tekwira.Dto.RatingRequest;
import com.project.tekwira.Dto.RatingResponse;

public interface RatingService {

    RatingResponse rateStadium(RatingRequest request);
}
