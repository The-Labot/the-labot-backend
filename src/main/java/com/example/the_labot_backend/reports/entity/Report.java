package com.example.the_labot_backend.reports.entity;

import com.example.the_labot_backend.authUser.entity.User;
import com.example.the_labot_backend.sites.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_reports")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 작업일보 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer; // 작성자 (현장관리자 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site; // 소속 현장

    @Column(nullable = false)
    private LocalDateTime createdAt; // 작성(보고)일자

    @Column(nullable = false, length = 100)
    private String workType; // 공종명 (예: 철근콘크리트, 설비, 전기 등)

    @Column(columnDefinition = "TEXT")
    private String todayWork; // 금일 작업사항

    @Column(columnDefinition = "TEXT")
    private String tomorrowPlan; // 명일 예정사항

    @Column(nullable = false)
    private int manpowerCount; // 투입 인원 수

    @Column(columnDefinition = "TEXT")
    private String materialStatus; // 자재 투입 현황

    @Column(columnDefinition = "TEXT")
    private String equipmentStatus; // 장비 투입 현황

    @Column(columnDefinition = "TEXT")
    private String specialNote; // 특이사항

    // 최초 저장 시 자동으로 생성일 설정
    @PrePersist // 처음 저장 시 createAt 자동 세팅
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
