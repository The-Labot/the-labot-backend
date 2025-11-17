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
    private String subject;
    private LocalDate educationDate;
    private String instructor;
    private EducationStatus status;

    public static EducationListResponse from(Education edu) {
        return EducationListResponse.builder()
                .id(edu.getId())
                .subject(edu.getSubject())
                .educationDate(edu.getEducationDate())
                .instructor(edu.getInstructor())
                .status(edu.getStatus())
                .build();
    }
}