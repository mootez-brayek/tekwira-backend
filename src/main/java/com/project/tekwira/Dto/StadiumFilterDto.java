package com.project.tekwira.Dto;

import com.project.tekwira.Models.FieldType;
import com.project.tekwira.Models.Governorate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StadiumFilterDto {
    private String name;
    private Governorate governorate;
    private FieldType fieldType;
}
