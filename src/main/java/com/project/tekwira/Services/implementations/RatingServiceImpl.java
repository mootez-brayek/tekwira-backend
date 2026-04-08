package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.RatingRequest;
import com.project.tekwira.Models.Rating;
import com.project.tekwira.Models.Stadium;
import com.project.tekwira.Models.User;
import com.project.tekwira.Repositories.RatingRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.RatingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RatingServiceImpl implements RatingService {

    private final StadiumRepository stadiumRepository;
    private final RatingRepository ratingRepository;

    public RatingServiceImpl(StadiumRepository stadiumRepository, RatingRepository ratingRepository) {
        this.stadiumRepository = stadiumRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    @Transactional
    public Rating rateStadium(RatingRequest request) {
        User player = JwtUtil.getCurrentUser();
        Stadium stadium = stadiumRepository.findById(request.getStadiumId())
                .orElseThrow(() -> new RuntimeException("Stadium not found"));
        Optional<Rating> existing = ratingRepository.findByPlayerAndStadium(player, stadium);
        Rating rating;
        if (existing.isPresent()) {
            rating = existing.get();
            rating.setScore(request.getScore());
            rating.setComment(request.getComment());
        }
        else {
            rating = new Rating();
            rating.setStadium(stadium);
            rating.setScore(request.getScore());
            rating.setComment(request.getComment());
            rating.setPlayer(player);
        }
        return ratingRepository.save(rating);
    }
}
