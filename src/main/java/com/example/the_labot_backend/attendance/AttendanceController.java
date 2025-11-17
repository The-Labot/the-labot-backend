package com.example.the_labot_backend.attendance;

import com.example.the_labot_backend.attendance.dto.ClockInOutRequestDto;
import com.example.the_labot_backend.attendance.dto.ObjectionRequestDto;
import com.example.the_labot_backend.users.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.the_labot_backend.attendance.dto.ClockInOutResponseDto;

import java.util.Map;

@RestController
@RequestMapping("/api/worker/attendance") // [★] 근로자(worker)용 API
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    /**
     * [★ 신규 ★]
     * 근로자가 출근 또는 퇴근 버튼을 눌렀을 때 호출
     * (GPS 좌표를 받아 서버에서 검증)
     */
    @PostMapping("/clock-in-out")
    public ResponseEntity<?> clockInOut(
            // [★] 현재 로그인한 유저 정보를 자동으로 가져옴
            @AuthenticationPrincipal User user,
            @RequestBody ClockInOutRequestDto dto) { // [★] 현재 GPS 좌표

        try {
            ClockInOutResponseDto resultDto = attendanceService.recordClockInOut(user, dto);

            // 출근인지 퇴근인지 확인
            String message = (resultDto.getClockOutTime() == null) ? "출근 처리 완료" : "퇴근 처리 완료";

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", message,
                    "data", resultDto
            ));

        } catch (IllegalStateException e) {
            // (예: "현장과 너무 멉니다", "이미 퇴근했습니다" 등)
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }
    // ▼▼▼▼▼ [★ 2. 이 메서드 블록 전체를 추가 ★] ▼▼▼▼▼
    /**
     * [신규] 근로자가 특정 출퇴근 기록에 "이의제기" 제출
     *
     * @param attendanceId 수정할 출퇴근 기록의 고유 ID (PK)
     * @param dto (예: { "message": "퇴근 버튼을 못 눌렀습니다" })
     */
    @PatchMapping("/{attendanceId}/object") // (PATCH /api/worker/attendance/101/object)
    public ResponseEntity<?> submitObjection(
            @AuthenticationPrincipal User user, // 현재 로그인한 근로자
            @PathVariable Long attendanceId,
            @RequestBody ObjectionRequestDto dto) {

        try {
            attendanceService.submitObjection(user, attendanceId, dto);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "이의제기가 성공적으로 제출되었습니다."
            ));
        } catch (IllegalStateException | EntityNotFoundException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }
    // ▲▲▲▲▲ [★ 2. 여기까지 추가 ★] ▲▲▲▲▲
}
