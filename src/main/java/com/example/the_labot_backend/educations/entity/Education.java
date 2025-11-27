package com.example.the_labot_backend.educations.entity;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.sites.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "safety_education")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    // 현장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    // 생성일
    private LocalDate createdDate;

    // --- 교육 정보 ---

    private String educationTitle;         // 교육 제목
    private LocalDate educationDate;       // 교육일자
    private String educationTime;          // 교육시간 (예: "09:00~10:00")
    private String educationPlace;         // 교육장소

    @Enumerated(EnumType.STRING)
    private EducationType educationType;   // 교육 구분 (과정)

    private String instructor;             // 강사명

    @Column(columnDefinition = "TEXT")
    private String content;                // 교육 상세내용

    @Enumerated(EnumType.STRING)
    private EducationStatus status;        // 예정 / 완료 여부

    // --- 특이사항 ---
    @Column(length = 1000)
    private String specialNote;            // 특이사항

    // --- 교육 결과 ---
    @Column(columnDefinition = "TEXT")
    private String result;                 // 교육 결과 간단 기록 (추가된 필드)
}
