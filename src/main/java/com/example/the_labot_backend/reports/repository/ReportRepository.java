package com.example.the_labot_backend.reports.repository;

import com.example.the_labot_backend.reports.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findBySiteIdAndCreatedAtBetween(
            Long siteId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Report> findAllBySite_IdOrderByCreatedAtDesc(Long siteId);

    long countBySite_IdAndCreatedAtAfter(Long siteId, LocalDateTime dateTime);

    // 2. 리스트: 24시간 이내 + 최신순 5개
    List<Report> findTop5BySite_IdAndCreatedAtAfterOrderByCreatedAtDesc(Long siteId, LocalDateTime dateTime);
}
