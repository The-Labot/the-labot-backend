package com.example.the_labot_backend.hazards.repository;

import com.example.the_labot_backend.hazards.entity.Hazard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HazardRepository extends JpaRepository<Hazard,Long> {
    List<Hazard> findAllBySite_Id(Long siteId);

    // 1. 오늘 날짜의 위험요소 신고 수 (Start ~ End 사이)
    long countBySite_IdAndReportedAtBetween(Long siteId, LocalDateTime start, LocalDateTime end);

    // 2. 최신 신고 5개 조회 (최근 활동용)
    List<Hazard> findTop5BySite_IdOrderByReportedAtDesc(Long siteId);
}
