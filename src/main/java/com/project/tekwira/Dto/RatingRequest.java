package com.project.tekwira.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingRequest {

    @NotNull
    private Long stadiumId;

    @Min(1)
    @Max(5)
    private int score;

    private String comment;
}
