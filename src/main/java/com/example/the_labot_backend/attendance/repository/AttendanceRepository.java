package com.example.the_labot_backend.attendance.repository;

import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.workers.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByWorkerAndDate(Worker worker, LocalDate date);
    
    // 특정 근로자, 기간 사이의 출퇴근 내역 조회
    List<Attendance> findByWorkerIdAndDateBetween(Long workerId, LocalDate start, LocalDate end);
    // [★ 추가] 특정 근로자의 모든 출퇴근 기록 조회 (날짜 내림차순 정렬)
    List<Attendance> findAllByWorkerOrderByDateDesc(Worker worker);

    @Query("SELECT DISTINCT a.worker.id FROM Attendance a " +
            "WHERE a.worker.user.site.id = :siteId " +
            "AND a.objectionMessage IS NOT NULL")
    List<Long> findWorkerIdsWithObjection(@Param("siteId") Long siteId);
}
