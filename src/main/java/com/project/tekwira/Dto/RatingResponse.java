package com.project.tekwira.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class RatingResponse {

    private Long id;
    private LocalDate date;
    private int score;
    private String comment;
    private String stadiumName;
    private String playerUsername;
}
