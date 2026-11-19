package com.project.tekwira.Dto;

import com.project.tekwira.Models.FieldType;
import com.project.tekwira.Models.Governorate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StadiumRequest {

    @NotBlank(message = "Stadium name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Governorate is required")
    private Governorate governorate;

    private Double latitude;
    private Double longitude;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    private FieldType fieldType;
    @NotNull(message = "Closing time is required")
    private LocalTime closingTime;

    @NotNull(message = "Price per game is required")
    private Double pricePerGame;

    @NotNull(message = "subscriptionPrice price is required")
    private Double subscriptionPrice;

    private String description;
    private List<MultipartFile> imageFiles;
    private MultipartFile logo;
}
