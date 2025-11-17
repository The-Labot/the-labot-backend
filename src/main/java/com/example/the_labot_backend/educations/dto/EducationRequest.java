package com.example.the_labot_backend.educations.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EducationRequest {

    private LocalDate educationDate;
    private String course;
    private int participants;
    private String subject;
    private String content;
    private String instructor;
    private String location;
    private String note;
}
