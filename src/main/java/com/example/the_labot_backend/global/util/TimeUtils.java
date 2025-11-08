package com.example.the_labot_backend.global.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 공통 시간 관련 유틸 클래스
 * 예: LocalDateTime → "방금 전", "5분 전", "2시간 전", "2025-11-07" 등으로 변환
 */
public class TimeUtils {

    /**
     * 주어진 시간(LocalDateTime)을 현재 시각과 비교하여
     * "방금 전", "N분 전", "N시간 전", "YYYY-MM-DD" 형태로 반환합니다.
     */
    public static String formatTimeAgo(LocalDateTime time) {
        if (time == null) return "-";

        Duration duration = Duration.between(time, LocalDateTime.now( ));
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        if (hours < 24) return hours + "시간 전";
        if (days < 7) return days + "일 전";
        return time.toLocalDate().toString(); // 7일 이상은 날짜로 표시
    }
}
