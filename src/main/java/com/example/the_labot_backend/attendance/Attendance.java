package com.example.the_labot_backend.attendance;

import com.example.the_labot_backend.workers.Worker;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 출퇴근 기록 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id") // DB에는 'worker_id' 컬럼이 생김
    private Worker worker;

    @Column(nullable = false)
    private LocalDate date; // 날짜 (예: 2025-10-31)

    private LocalTime clockInTime; // 출근 시간 (예: 08:15)
    private LocalTime clockOutTime; // 퇴근 시간 (예: 18:00)

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status; // 상태 (예: 정상, 지각)

    private String objectionMessage; // 이의제기 내용
}
