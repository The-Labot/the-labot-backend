package com.example.the_labot_backend.educations.dto;

import com.example.the_labot_backend.educations.entity.Education;
import com.example.the_labot_backend.educations.entity.EducationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class EducationListResponse {

    private Long id;
    private String educationTitle;
    private LocalDate educationDate;
    private String educationPlace;
    private EducationStatus status;

    public static EducationListResponse from(Education edu) {
        return EducationListResponse.builder()
                .id(edu.getId())
                .educationTitle(edu.getEducationTitle())
                .educationDate(edu.getEducationDate())
                .educationPlace(edu.getEducationPlace())
                .status(edu.getStatus())
                .build();
    }
}