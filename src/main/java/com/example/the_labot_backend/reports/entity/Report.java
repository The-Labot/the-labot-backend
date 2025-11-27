package com.example.the_labot_backend.reports.entity;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.sites.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private Long id;
    // 작업일보 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;
    // 작성자(현장관리자 또는 담당 직원)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    // 해당 작업일보가 속한 건설 현장

    @Column(nullable = false)
    private LocalDateTime createdAt;
    // 작성일자 (최초 생성 시 자동 입력)

    // ===========================
    // 사용자 입력 데이터 영역
    // ===========================

    @Column(nullable = false, length = 100)
    private String workType;
    // 공종명 (예: 철근콘크리트, 비계, 타설, 전기, 설비 등)

    private LocalDate workDate;
    // 실제 작업이 수행된 날짜

    private String todayWork;
    // 금일 작업내용 (예: "2층 슬래브 철근 조립 60% 진행")

    private String tomorrowPlan;
    // 명일 작업 예정 사항 (예: "거푸집 설치 및 잔여 철근 배근 예정")

    private String workLocation;
    // 작업 위치 (층/동/구역 등)

    @Column(columnDefinition = "TEXT")
    private String specialNote;
    // 특이사항 (안전, 품질, 문제 발생, 지연 사유 등)

    // 최초 저장 시 자동으로 생성일 설정
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

