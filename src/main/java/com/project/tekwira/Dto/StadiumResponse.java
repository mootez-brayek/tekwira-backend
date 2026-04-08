package com.project.tekwira.Dto;

import com.project.tekwira.Models.FieldType;
import com.project.tekwira.Models.User;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StadiumResponse {

    private Long id;
    private List<String> imageUrls;
    private String logo;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private FieldType fieldType;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Double pricePerGame;
    private Double subscriptionPrice;
    private Double averageRating;
    private Long ratingsCount;
    private User owner;
    private Boolean isActive;

}
