package com.example.the_labot_backend.reports.repository;

import com.example.the_labot_backend.reports.entity.ReportWorker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportWorkerRepository extends JpaRepository<ReportWorker, Long> {

    // 특정 작업일보에 속한 근로자 목록 조회
    List<ReportWorker> findByReportId(Long reportId);

    // 작업일보 삭제 시 연관된 근로자 삭제
    void deleteByReportId(Long reportId);
}
