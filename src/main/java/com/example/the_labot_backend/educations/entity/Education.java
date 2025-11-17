package com.example.the_labot_backend.educations.entity;

import com.example.the_labot_backend.sites.Site;
import com.example.the_labot_backend.authUser.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;

    // 수정일
    private LocalDateTime updatedAt;

    // 시행 날짜 (교육 실제 날짜)
    @Column(nullable = false)
    private LocalDate educationDate;

    // 과정 (예: 정기교육, 특별교육)
    @Column(nullable = false)
    private String course;

    // 인원 수
    @Column(nullable = false)
    private int participants;

    // 과목
    @Column(nullable = false)
    private String subject;

    // 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 강사명
    @Column(nullable = false)
    private String instructor;

    // 장소
    @Column(nullable = false)
    private String location;

    // 특이사항
    @Column(columnDefinition = "TEXT")
    private String note;

    // 예정 / 완료 여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EducationStatus status;

    // 완료일 (완료된 다음날 자동삭제 로직에서 사용)
    private LocalDate completedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();

        // 시행일이 오늘 이후면 예정, 오늘 또는 과거면 완료
        if (educationDate.isAfter(LocalDate.now())) {
            status = EducationStatus.PLANNED;
        } else {
            status = EducationStatus.COMPLETED;
            completedAt = educationDate;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
