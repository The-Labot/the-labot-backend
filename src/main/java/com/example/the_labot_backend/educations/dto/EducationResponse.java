package com.example.the_labot_backend.educations.dto;

import com.example.the_labot_backend.educations.entity.Education;
import com.example.the_labot_backend.educations.entity.EducationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EducationResponse {

    private Long id;
    private LocalDate educationDate;
    private String course;
    private int participants;
    private String subject;
    private String content;
    private String instructor;
    private String location;
    private String note;
    private EducationStatus status;
    private LocalDate completedAt;
    private String writerName;
    private LocalDateTime createdAt;

    public static EducationResponse from(Education edu) {
        return EducationResponse.builder()
                .id(edu.getId())
                .educationDate(edu.getEducationDate())
                .course(edu.getCourse())
                .participants(edu.getParticipants())
                .subject(edu.getSubject())
                .content(edu.getContent())
                .instructor(edu.getInstructor())
                .location(edu.getLocation())
                .note(edu.getNote())
                .status(edu.getStatus())
                .completedAt(edu.getCompletedAt())
                .writerName(edu.getWriter().getName())
                .createdAt(edu.getCreatedAt())
                .build();
    }
}
