package com.example.the_labot_backend.educations.dto;

import com.example.the_labot_backend.educations.entity.EducationStatus;
import com.example.the_labot_backend.educations.entity.EducationType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationCreateRequest {
    // 교육 정보
    private String educationTitle;
    private LocalDate educationDate;
    private String educationTime;
    private String educationPlace;
    private EducationType educationType;

    private String instructor;
    private String content;
    private EducationStatus status;

    private String specialNote;   // 특이사항
    private String result;        // 교육 결과
    // 교육 대상자 workerId 리스트
    private List<Long> participantIds;
}
