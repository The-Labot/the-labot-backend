package com.example.the_labot_backend.notices;

import com.example.the_labot_backend.notices.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 현장별 공지 조회 (작성자의 현장 기준)
    // 고정된 것 먼저, 최신순 정렬
    @Query("SELECT n FROM Notice n WHERE n.writer.site.id = :siteId ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findAllBySiteIdOrderByPinnedDesc(Long siteId);
}
