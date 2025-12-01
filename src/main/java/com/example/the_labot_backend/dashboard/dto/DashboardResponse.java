package com.example.the_labot_backend.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private Summary summary;                // 상단 통계 카드
    private List<RecentActivity> activities; // 최근 활동 리스트 (혼합)

    @Getter
    @Builder
    public static class Summary {
        private long todayHazardCount;  // 오늘 위험 신고 수
        private long ongoingWorkCount;  // 진행 중인 작업 (작업일보)
        private long workerCount;       // 현장 출근 근로자 수
    }

    @Getter
    @Builder
    @AllArgsConstructor // 빌더 패턴 사용 시 필수
    @NoArgsConstructor  // JSON 변환 시 필수
    public static class RecentActivity {
        private Long id;
        private String type;        // "HAZARD", "REPORT", "ATTENDANCE"
        private String title;       // 제목
        private String description; // 설명
        private String timeAgo;     // 시간 (예: "방금 전")
        private String status;      // 상태 태그 (예: "긴급", "출근")

        // 정렬을 위해 시간 원본 저장 (클라이언트에게는 안 보냄)
        @JsonIgnore
        private LocalDateTime originalTime;
    }
}
