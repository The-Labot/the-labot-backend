package com.example.the_labot_backend.attendanceRecord.service;

import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.attendance.repository.AttendanceRepository;
import com.example.the_labot_backend.attendanceRecord.dto.*;
import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import com.example.the_labot_backend.attendanceRecord.repository.AttendanceRecordRepository;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceRecordService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public MonthlyAttendanceRecordResponse getMonthlyRecords(Long adminId,
                                                             Long siteId,
                                                             Long userId,
                                                             int year,
                                                             int month) {

        // -----------------------
        // 1) 권한 검증
        // -----------------------
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다. adminId=" + adminId));

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다. siteId=" + siteId));

        if (!admin.getHeadOffice().getId().equals(site.getHeadOffice().getId())) {
            throw new ForbiddenException("해당 현장에 접근할 권한이 없습니다.");
        }

        User worker = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("근로자를 찾을 수 없습니다. userId=" + userId));

        if (!worker.getSite().getId().equals(siteId)) {
            throw new ForbiddenException("해당 현장에 소속된 근로자가 아닙니다.");
        }

        // -----------------------
        // 2) 기간 설정
        // -----------------------
        LocalDate start = LocalDate.of(year, month, 1); // 해당 년월 첫날
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth()); // 해당 년울 마지막 날

        // -----------------------
        // 3) 데이터 조회
        // -----------------------
        List<Attendance> attendances =
                attendanceRepository.findByWorkerIdAndDateBetween(worker.getId(), start, end);

        List<AttendanceRecord> records =
                attendanceRecordRepository.findByWorkerIdAndWorkDateBetween(worker.getId(), start, end);

        // -----------------------
        // 4) Daily DTO 구성
        // -----------------------
        List<DailyAttendanceRecord> dailyList = new ArrayList<>();
        double monthlyTotalWork = 0;
        double monthlyManHour = 0;

        for (int day = 1; day <= start.lengthOfMonth(); day++) {

            LocalDate current = LocalDate.of(year, month, day);

            Attendance attendance = attendances.stream()
                    .filter(a -> a.getDate().equals(current))
                    .findFirst()
                    .orElse(null);

            AttendanceRecord record = records.stream()
                    .filter(r -> r.getWorkDate().equals(current))
                    .findFirst() // 남은 값중 첫번째 값 꺼내기 (1개밖에없음)
                    .orElse(null); // 없으면 null 반환

            if (record == null) {
                continue;
            }

            double manHour = record.getManHour();
            double totalWork = record.getActualTotalWork();

            monthlyTotalWork += totalWork;
            monthlyManHour += manHour;

            dailyList.add(
                    DailyAttendanceRecord.builder()
                            .date(current.toString())
                            .clockIn(attendance != null ? attendance.getClockInTime().toString() : null)
                            .clockOut(attendance != null ? attendance.getClockOutTime().toString() : null)

                            .totalWork(record.getActualTotalWork())
                            .basicWork(record.getActualBasicWork())
                            .extendedWork(record.getActualExtendedWork())
                            .overtimeWork(record.getActualOvertimeWork())
                            .nightWork(record.getActualNightWork())
                            .holidayWork(record.getActualHolidayWork())

                            .unitPrice(record.getUnitPrice())
                            .manHour(manHour)
                            .build()
            );
        }

        // -----------------------
        // 5) 최종 응답
        // -----------------------
        return MonthlyAttendanceRecordResponse.builder()
                .monthlyTotalWork(monthlyTotalWork)
                .monthlyTotalManHour(monthlyManHour)
                .dailyRecords(dailyList)
                .build();
    }

    // 근로 시간 수정
    @Transactional
    public AttendanceRecordResponse updateRecord(Long adminId,
                                                 Long siteId,
                                                 Long userId,
                                                 AttendanceRecordUpdateRequest req) {

        // -----------------------
        // 1) 권한 검증
        // -----------------------
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. adminId:" + adminId));

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다. siteId:" + siteId));

        // 본사관리자의 현장과 현장Id가 다른 경우
        if (!admin.getHeadOffice().getId().equals(site.getHeadOffice().getId())) {
            throw new RuntimeException("해당 현장에 접근할 권한이 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId:" + userId));

        // 현장과 근로자의 현장이 다른 경우
        if (!user.getSite().getId().equals(siteId)) {
            throw new RuntimeException("해당 현장에 소속된 근로자가 아닙니다.");
        }

        // userId와 workerId는 동일
        Worker worker = workerRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("근로자를 찾을 수 없습니다."));
        
        LocalDate targetDate = req.getDate();

        AttendanceRecord record = attendanceRecordRepository
                .findByWorkerIdAndWorkDate(worker.getId(), targetDate)
                .orElseThrow(() -> new NotFoundException("해당 날짜의 근태 기록이 없습니다. date=" + targetDate));

        // 총 근로
        double totalActual =
                req.getActualBasicWork()
                        + req.getActualExtendedWork()
                        + req.getActualOvertimeWork()
                        + req.getActualNightWork()
                        + req.getActualHolidayWork();

        record.setActualTotalWork(totalActual);
        record.setActualBasicWork(req.getActualBasicWork());
        record.setActualExtendedWork(req.getActualExtendedWork());
        record.setActualOvertimeWork(req.getActualOvertimeWork());
        record.setActualNightWork(req.getActualNightWork());
        record.setActualHolidayWork(req.getActualHolidayWork());

        double manHour = (
                req.getActualBasicWork()
                        + req.getActualExtendedWork()
                        + req.getActualOvertimeWork()
                        + req.getActualNightWork()
                        + req.getActualHolidayWork()
        ) / 8.0;

        record.setManHour(manHour);
        record.setUnitPrice(req.getUnitPrice());

        attendanceRecordRepository.save(record);

        // 8) 응답 반환
        return AttendanceRecordResponse.from(record);
    }

    @Transactional(readOnly = true)
    public AttendanceMonthlyResponse getAttendanceMonthly(Long siteId, Long workerId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<AttendanceRecord> records =
                attendanceRecordRepository.findByWorkerIdAndMonth(workerId, start, end);

        List<AttendanceDailyResponse> dailyList = records.stream()
                .map(r -> AttendanceDailyResponse.builder()
                        .date(r.getWorkDate())
                        .manHour(r.getManHour())
                        .build())
                .toList();

        return AttendanceMonthlyResponse.builder()
                .year(year)
                .month(month)
                .records(dailyList)
                .build();
    }
}
