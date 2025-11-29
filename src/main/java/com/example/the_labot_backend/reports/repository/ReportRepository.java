package com.example.the_labot_backend.reports.repository;

import com.example.the_labot_backend.reports.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findBySiteIdAndCreatedAtBetween(
            Long siteId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Report> findAllBySite_IdOrderByCreatedAtDesc(Long siteId);
}
