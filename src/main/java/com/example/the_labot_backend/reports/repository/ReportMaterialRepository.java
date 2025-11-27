package com.example.the_labot_backend.reports.repository;

import com.example.the_labot_backend.reports.entity.ReportMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportMaterialRepository extends JpaRepository<ReportMaterial, Long> {

    // 특정 작업일보에 속한 자재 목록
    List<ReportMaterial> findByReportId(Long reportId);

    // 작업일보 삭제 시 연관 자재 삭제
    void deleteByReportId(Long reportId);
}
