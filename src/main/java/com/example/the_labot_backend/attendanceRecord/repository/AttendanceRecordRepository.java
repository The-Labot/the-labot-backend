package com.example.the_labot_backend.attendanceRecord.repository;

import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    // 해당 연도와 달을 가진 객체 조회
    @Query("""
       SELECT ar FROM AttendanceRecord ar
       WHERE ar.worker.id = :workerId
       AND FUNCTION('YEAR', ar.workDate) = :year
       AND FUNCTION('MONTH', ar.workDate) = :month
       """)
    List<AttendanceRecord> findMonthlyRecords(Long workerId, int year, int month);

    @Query("""
       SELECT ar FROM AttendanceRecord ar
       WHERE ar.worker.user.site.id = :siteId
       AND FUNCTION('YEAR', ar.workDate) = :year
       AND FUNCTION('MONTH', ar.workDate) = :month
       """)
    List<AttendanceRecord> findMonthlyRecordsBySite(Long siteId, int year, int month);

    // start~end 사이의 객체 조회
    List<AttendanceRecord> findByWorkerIdAndWorkDateBetween(Long workerId, LocalDate start, LocalDate end);

    Optional<AttendanceRecord> findByWorkerIdAndWorkDate(Long workerId, LocalDate workDate);
}
