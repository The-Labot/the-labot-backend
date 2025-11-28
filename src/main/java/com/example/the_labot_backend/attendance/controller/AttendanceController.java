package com.example.the_labot_backend.attendance.controller;

import com.example.the_labot_backend.attendance.dto.ClockInOutRequestDto;
import com.example.the_labot_backend.attendance.dto.ObjectionRequestDto;
import com.example.the_labot_backend.attendance.service.AttendanceService;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.the_labot_backend.attendance.dto.ClockInOutResponseDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/worker/attendance") // [★] 근로자(worker)용 API
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    /**
     * [★ 신규 ★]
     * 근로자가 출근 또는 퇴근 버튼을 눌렀을 때 호출
     * (GPS 좌표를 받아 서버에서 검증)
     */
    @PostMapping("/clock-in-out")
    public ResponseEntity<?> clockInOut(@RequestBody ClockInOutRequestDto dto) { // [★] 현재 GPS 좌표
        User user = getCurrentUser();

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

    /**
     * [신규] 나의 출퇴근 내역 전체 조회
     * GET /api/worker/attendance
     */
    @GetMapping
    public ResponseEntity<?> getMyAttendanceHistory() {
        User user = getCurrentUser(); // 현재 로그인한 유저(근로자) 정보

        try {
            List<ClockInOutResponseDto> history = attendanceService.getMyAttendanceHistory(user);

            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "출퇴근 내역 조회 성공",
                    "data", history
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * [신규] 근로자가 특정 출퇴근 기록에 "이의제기" 제출
     *
     * @param attendanceId 수정할 출퇴근 기록의 고유 ID (PK)
     * @param dto (예: { "message": "퇴근 버튼을 못 눌렀습니다" })
     */
    @PatchMapping("/{attendanceId}/object") // (PATCH /api/worker/attendance/101/object)
    public ResponseEntity<?> submitObjection(
            @PathVariable Long attendanceId,
            @RequestBody ObjectionRequestDto dto) {
        User user = getCurrentUser();

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


    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // (혹시 모를 예외 처리)
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        try {
            Long userId = Long.parseLong(auth.getName()); // 토큰의 subject(ID) 파싱
            return userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. ID: " + userId));
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 토큰 ID 형식입니다.");
        }
    }
}
