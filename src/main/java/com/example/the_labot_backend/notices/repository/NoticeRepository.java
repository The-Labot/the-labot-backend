package com.example.the_labot_backend.notices.repository;

import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 현장별 공지 조회 (작성자의 현장 기준)
    // 고정된 것 먼저, 최신순 정렬
    List<Notice> findAllBySite_IdOrderByPinnedDesc(Long siteId);

    // ▼▼▼ [★ 1. 근로자용 필터링 조회 쿼리 추가 ★] ▼▼▼
    // 조건:
    // 1. 내 현장(siteId)의 공지여야 함
    // 2. (선택) 카테고리가 일치해야 함 (null이면 전체 조회)
    // 3. 작성일이 지정된 기간(startDate ~ endDate) 사이여야 함
    // 정렬: 1.고정(pinned) -> 2.긴급(urgent) -> 3.최신순(createdAt)
    @Query("SELECT n FROM Notice n " +
            "WHERE n.site.id = :siteId " +
            "AND (:category IS NULL OR n.category = :category) " +
            "AND n.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY n.pinned DESC, n.urgent DESC, n.createdAt DESC")
    List<Notice> findNoticesForWorker(
            @Param("siteId") Long siteId,
            @Param("category") NoticeCategory category, // null이면 전체
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    // ▲▲▲ [★ 1. 여기까지 추가] ▲▲▲
}
