package com.example.the_labot_backend.hazards.repository;

import com.example.the_labot_backend.hazards.entity.Hazard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HazardRepository extends JpaRepository<Hazard,Long> {
    List<Hazard> findAllBySite_Id(Long siteId);

    // 1. 카운트: 오늘 하루 (Start ~ End)
    long countBySite_IdAndReportedAtBetween(Long siteId, LocalDateTime start, LocalDateTime end);

    // 2. 리스트: 오늘 하루 (Start ~ End) + 최신순 5개
    List<Hazard> findTop5BySite_IdAndReportedAtBetweenOrderByReportedAtDesc(Long siteId, LocalDateTime start, LocalDateTime end);
}
