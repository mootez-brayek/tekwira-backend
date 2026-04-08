package com.project.tekwira.Services.implementations;

import com.project.tekwira.Dto.RatingStats;
import com.project.tekwira.Dto.StadiumFilterDto;
import com.project.tekwira.Dto.StadiumRequest;
import com.project.tekwira.Dto.StadiumResponse;
import com.project.tekwira.Models.Stadium;
import com.project.tekwira.Models.User;
import com.project.tekwira.Repositories.RatingRepository;
import com.project.tekwira.Repositories.StadiumRepository;
import com.project.tekwira.Security.JwtUtil;
import com.project.tekwira.Services.FileStorageService;
import com.project.tekwira.Services.StadiumService;
import com.project.tekwira.Services.StadiumSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumServiceImpl implements StadiumService {

    private final StadiumRepository stadiumRepository;
    private final StadiumSlotService stadiumSlotService;
    private final RatingRepository ratingRepository;
    private final FileStorageService fileStorageService;

    @Override
    public StadiumResponse createStadium(StadiumRequest request) throws IOException {
        User owner = JwtUtil.getCurrentUser();
        List<String> urls = fileStorageService.storeFiles(request.getImageUrls());

        String logoUrl = request.getLogo() != null ? fileStorageService.storeFile(request.getLogo()) : null;

        if (stadiumRepository.existsByOwnerAndName(owner, request.getName())) {
            throw new RuntimeException("You already have a stadium with this name");
        }

        Stadium.StadiumBuilder builder = Stadium.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .openingTime(request.getOpeningTime())
                .closingTime(request.getClosingTime())
                .pricePerGame(request.getPricePerGame())
                .description(request.getDescription())
                .imageUrls(urls)
                .subscriptionPrice(request.getSubscriptionPrice())
                .logo(logoUrl)
                .owner(owner);

            if (request.getFieldType() != null) {
                builder.fieldType(request.getFieldType());
            }

            Stadium stadium = builder.build();
        Stadium savedStadium = stadiumRepository.save(stadium);

        stadiumSlotService.createSlotsForStadium(savedStadium.getId());

        return mapToResponse(savedStadium);
    }

    @Override
    @Transactional
    public StadiumResponse updateStadium(Long stadiumId, StadiumRequest request) {
        User owner = JwtUtil.getCurrentUser();
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new RuntimeException("Stadium not found"));

        if (!stadium.getOwner().getUserId().equals(owner.getUserId())) {
            throw new RuntimeException("Not authorized to update this stadium");
        }

        // Update fields if they are not null/blank
        if (request.getName() != null && !request.getName().isBlank())
            stadium.setName(request.getName());
        if (request.getAddress() != null && !request.getAddress().isBlank())
            stadium.setAddress(request.getAddress());
        if (request.getLatitude() != null) stadium.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) stadium.setLongitude(request.getLongitude());
        if (request.getOpeningTime() != null) stadium.setOpeningTime(request.getOpeningTime());
        if (request.getClosingTime() != null) stadium.setClosingTime(request.getClosingTime());
        if (request.getPricePerGame() != null) stadium.setPricePerGame(request.getPricePerGame());
        if (request.getDescription() != null) stadium.setDescription(request.getDescription());

        Stadium updateStadium = stadiumRepository.save(stadium);
        return mapToResponse(updateStadium);
    }

    @Override
    @Transactional
    public void deleteStadium(Long stadiumId) {
        User owner = JwtUtil.getCurrentUser();
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new RuntimeException("Stadium not found"));

        if (!stadium.getOwner().getUserId().equals(owner.getUserId())) {
            throw new RuntimeException("Not authorized to delete this stadium");
        }

        stadiumRepository.delete(stadium);
    }

    @Override
    public List<StadiumResponse> listAllStadiums(StadiumFilterDto filter) {
        return stadiumRepository.findAll()
                .stream()
                .filter(s -> filter.getAddress() == null || filter.getName().isBlank() || s.getAddress().equalsIgnoreCase(filter.getAddress()))
                .filter(s -> (filter.getName() == null || filter.getName().isBlank() ||
                                s.getName().toLowerCase().contains(filter.getName().toLowerCase())))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<StadiumResponse> listStadiumsByOwner(User owner, StadiumFilterDto filter) {
        return stadiumRepository.findByOwner(owner)
                .stream()
                .filter(s -> filter.getAddress() == null || filter.getName().isBlank() || s.getAddress().equalsIgnoreCase(filter.getAddress()))
                .filter(s -> (filter.getName() == null || filter.getName().isBlank() ||
                        s.getName().toLowerCase().contains(filter.getName().toLowerCase())))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public StadiumResponse getStadiumById(Long stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new RuntimeException("Stadium not found"));
        return mapToResponse(stadium);
    }



    @Override
    public void toggleActive(Long stadiumId) {
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new RuntimeException("Stadium not found"));
        stadium.setActive(!stadium.isActive());
        stadiumRepository.save(stadium);
    }


    @Override
    public List<StadiumResponse> getTopRatedStadiums(StadiumFilterDto filter) {
        return stadiumRepository.findTopRatedStadiums()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<StadiumResponse> getAllActiveStadiums(StadiumFilterDto filter) {
        return stadiumRepository.getAllActiveStadiums()
                .stream()
                .filter(s -> filter.getAddress() == null || filter.getName().isBlank() || s.getAddress().equalsIgnoreCase(filter.getAddress()))
                .filter(s -> (filter.getName() == null || filter.getName().isBlank() ||
                        s.getName().toLowerCase().contains(filter.getName().toLowerCase())))
                .map(this::mapToResponse).toList();
    }

    public StadiumResponse mapToResponse(Stadium stadium) {

        RatingStats stats = ratingRepository.getRatingStatsForStadium(stadium.getId());

        double avg = stats != null && stats.getAvgRating() != null ? stats.getAvgRating() : 0.0;
        long count = stats != null ? stats.getRatingsCount() : 0;

        return new StadiumResponse(
                stadium.getId(),
                stadium.getImageUrls(),
                stadium.getLogo(),
                stadium.getName(),
                stadium.getDescription(),
                stadium.getAddress(),
                stadium.getLatitude(),
                stadium.getLongitude(),
                stadium.getFieldType(),
                stadium.getOpeningTime(),
                stadium.getClosingTime(),
                stadium.getPricePerGame(),
                stadium.getSubscriptionPrice(),
                avg,
                count,
                stadium.getOwner(),
                stadium.isActive()
        );
    }
}
