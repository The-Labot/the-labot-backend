package com.example.the_labot_backend.attendanceRecord.repository;

import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 해당 siteId와 년/월에 맞는 기록 가져오기 
    @Query("""
       SELECT ar FROM AttendanceRecord ar
       WHERE ar.worker.user.site.id = :siteId
       AND FUNCTION('YEAR', ar.workDate) = :year
       AND FUNCTION('MONTH', ar.workDate) = :month
       """)
    List<AttendanceRecord> findMonthlyRecordsBySite(Long siteId, int year, int month);

    // 특정 날짜 사이 총 공수 가져오기
    @Query("""
        SELECT COALESCE(SUM(a.manHour), 0)
        FROM AttendanceRecord a
        WHERE a.worker.id = :workerId
        AND a.workDate >= :startDate
        AND a.workDate <= :endDate
    """)
    Double getMonthlyTotalManHour(
            @Param("workerId") Long workerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 특정 날짜 사이 기록 가져오기
    @Query("""
    SELECT a FROM AttendanceRecord a
    WHERE a.worker.id = :workerId
    AND a.workDate >= :start
    AND a.workDate <= :end
""")
    List<AttendanceRecord> findByWorkerIdAndMonth(
            @Param("workerId") Long workerId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // start~end 사이의 객체 조회
    List<AttendanceRecord> findByWorkerIdAndWorkDateBetween(Long workerId, LocalDate start, LocalDate end);

    Optional<AttendanceRecord> findByWorkerIdAndWorkDate(Long workerId, LocalDate workDate);
}
