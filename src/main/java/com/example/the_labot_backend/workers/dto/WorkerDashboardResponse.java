package com.example.the_labot_backend.workers.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WorkerDashboardResponse {
    // 1. 상단 통계 (퇴직자 제외)
    private long totalCount;      // 전체 (Active + Waiting)
    private long activeCount;     // 근무중
    private long waitingCount;    // 대기중
    private long objectionCount;  // 이의제기 수 (사람 수 기준)

    // 2. 좌측 근로자 목록 (기존 DTO 재사용)
    private List<WorkerListResponse> workers;
}
