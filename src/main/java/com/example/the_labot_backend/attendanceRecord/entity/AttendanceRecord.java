package com.example.the_labot_backend.attendanceRecord.entity;

import com.example.the_labot_backend.workers.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 근로자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    // 근무일자
    @Column(nullable = false)
    private LocalDate workDate;

    // =============================
    // 실제시간 (출퇴근 기록 기반)
    // =============================
    private Double actualTotalWork;       // 실제 총근로
    private Double actualBasicWork;       // 실제 기본근로
    private Double actualExtendedWork;    // 실제 연장근로
    private Double actualOvertimeWork;    // 실제 초과근로
    private Double actualNightWork;       // 실제 야간근로
    private Double actualHolidayWork;     // 실제 휴일근로

    // =============================
    // 단가 및 공수
    // =============================
    private Long unitPrice;   // 기준단가
    private Double manHour;    // 공수
}

