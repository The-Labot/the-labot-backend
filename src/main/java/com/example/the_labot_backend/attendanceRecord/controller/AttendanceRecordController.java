package com.example.the_labot_backend.attendanceRecord.controller;

import com.example.the_labot_backend.attendanceRecord.dto.AttendanceRecordResponse;
import com.example.the_labot_backend.attendanceRecord.dto.AttendanceRecordUpdateRequest;
import com.example.the_labot_backend.attendanceRecord.dto.MonthlyAttendanceRecordResponse;
import com.example.the_labot_backend.attendanceRecord.service.AttendanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/sites/{siteId}/workers/{userId}")
@RequiredArgsConstructor
public class AttendanceRecordController {

    private final AttendanceRecordService attendanceRecordService;

    @GetMapping
    public ResponseEntity<?> getRecords(
            @PathVariable Long siteId,
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        MonthlyAttendanceRecordResponse response = attendanceRecordService.getMonthlyRecords(adminId, siteId, userId, year, month);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "근로자 근로내역 조회 성공",
                "data", response
        ));
    }

    @PutMapping
    public ResponseEntity<?> createRecord(@PathVariable Long siteId,
                                                 @PathVariable Long userId,
                                                 @RequestBody AttendanceRecordUpdateRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        AttendanceRecordResponse response = attendanceRecordService.updateRecord(adminId,siteId,userId,request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "근로자 근로내역 수정 성공",
                "data", response
        ));
    }
}
