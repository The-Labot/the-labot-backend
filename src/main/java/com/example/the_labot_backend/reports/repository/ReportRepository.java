package com.example.the_labot_backend.reports.repository;

import com.example.the_labot_backend.reports.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("""
        SELECT r FROM Report r
        WHERE r.site.id = :siteId
          AND r.createdAt BETWEEN :start AND :end
        """)
    List<Report> findDailyReports(
            @Param("siteId") Long siteId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Report> findAllBySite_IdOrderByCreatedAtDesc(Long siteId);

    // 1. 오늘 날짜의 작업일보 수
    long countBySite_IdAndWorkDate(Long siteId, LocalDate workDate);

    // 2. 최신 작업일보 5개 조회 (작성일 기준 내림차순)
    List<Report> findTop5BySite_IdOrderByCreatedAtDesc(Long siteId);
}
