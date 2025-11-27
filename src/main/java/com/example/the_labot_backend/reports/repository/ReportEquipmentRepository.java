package com.example.the_labot_backend.reports.repository;

import com.example.the_labot_backend.reports.entity.ReportEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportEquipmentRepository extends JpaRepository<ReportEquipment, Long> {

    // 특정 작업일보에 속한 장비 목록
    List<ReportEquipment> findByReportId(Long reportId);

    // 작업일보 삭제 시 연관 장비 삭제
    void deleteByReportId(Long reportId);
}
