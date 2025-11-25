package com.example.the_labot_backend.attendance.repository;

import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.workers.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByWorkerAndDate(Worker worker, LocalDate date);
    
    // 특정 근로자, 기간 사이의 출퇴근 내역 조회
    List<Attendance> findByWorkerIdAndDateBetween(Long workerId, LocalDate start, LocalDate end);
}
