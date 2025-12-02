package com.example.the_labot_backend.attendance.service;

import com.example.the_labot_backend.attendance.dto.ClockInOutRequestDto;
import com.example.the_labot_backend.attendance.dto.ClockInOutResponseDto;
import com.example.the_labot_backend.attendance.dto.ObjectionRequestDto;
import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.attendance.entity.AttendanceStatus;
import com.example.the_labot_backend.attendance.repository.AttendanceRepository;
import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import com.example.the_labot_backend.attendanceRecord.repository.AttendanceRecordRepository;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    // ▼▼▼ [★ 1. 기준 시간 2개 설정] ▼▼▼
    private static final LocalTime NINE_AM = LocalTime.of(9, 0);  // 9시 00분 00초
    private static final LocalTime TEN_AM = LocalTime.of(10, 0); // 10시 00분 00초


    // 현장 허용 거리 (예: 100미터)
    private static final double ALLOWED_DISTANCE_METERS = 100.0;

    @Transactional
    public ClockInOutResponseDto recordClockInOut(User user, ClockInOutRequestDto dto) {

        Worker worker = user.getWorker();
        if (worker == null) {
            throw new IllegalStateException("근로자 정보가 없는 유저입니다.");
        }

        Site site = user.getSite();
        if (site == null || site.getLatitude() == null || site.getLongitude() == null) {
            throw new IllegalStateException("배정된 현장이 없거나, 현장에 GPS 좌표가 등록되지 않았습니다.");
        }

        // --- 1. [핵심] GPS 위치 검증 ---
        double distance = calculateDistance(
                dto.getLatitude(), dto.getLongitude(), // [★] 사람 위치 (DTO)
                site.getLatitude(), site.getLongitude() // [★] 현장 위치 (Site)
        );

        if (distance > ALLOWED_DISTANCE_METERS) {
            throw new IllegalStateException("현장과 너무 멉니다. (거리: " + (int)distance + "m)");
        }

        // --- 2. 오늘 날짜의 출근 기록 찾기 ---
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now(); // [★] 버튼 누른 현재 시간

        Optional<Attendance> existingRecord = attendanceRepository.findByWorkerAndDate(worker, today);

        if (existingRecord.isEmpty()) {

            // --- 3. [출근 처리] ---

            // ▼▼▼ [★ 2. 10시/9시 기준 판별 로직 수정] ▼▼▼
            AttendanceStatus status;
            if (now.isBefore(NINE_AM)) {
                status = AttendanceStatus.PRESENT; // 08:59:59 까지
            } else {
                status = AttendanceStatus.LATE;    // 09:00:00 부터 쭉~ (지각)
            }




            Attendance newRecord = Attendance.builder()
                    .worker(worker)
                    .date(today)
                    .clockInTime(now) // [★] 그냥 현재 시간 저장
                    .status(status)
                    .build();
            Attendance savedRecord = attendanceRepository.save(newRecord);
            worker.setStatus(WorkerStatus.ACTIVE);
            return ClockInOutResponseDto.fromEntity(savedRecord);

        } else {
        // --- 4. [퇴근 처리] ---
        Attendance record = existingRecord.get();
        if (record.getClockOutTime() != null) {
            throw new IllegalStateException("이미 퇴근 처리가 완료되었습니다.");
        }
        record.setClockOutTime(now); // [★] 그냥 현재 시간 저장
        Attendance savedRecord = attendanceRepository.save(record);
        worker.setStatus(WorkerStatus.WAITING);

        // attnedanceRecord 값 추가
        AttendanceRecord attendanceRecode = calculateRecord(record, Long.valueOf(worker.getSalary()));
        attendanceRecordRepository.save(attendanceRecode);
        calculateRecord(record, Long.valueOf(worker.getSalary()));

        return ClockInOutResponseDto.fromEntity(savedRecord);
    }
    }



    /**
     * [신규] 로그인한 근로자의 모든 출퇴근 내역 조회 (최신순)
     */
    @Transactional(readOnly = true) // 조회 전용이므로 readOnly 권장
    public List<ClockInOutResponseDto> getMyAttendanceHistory(User user) {

        Worker worker = user.getWorker();
        if (worker == null) {
            throw new IllegalStateException("근로자 정보가 없는 유저입니다.");
        }

        // 1. Repository에서 근로자의 모든 기록을 최신순으로 가져옴
        List<Attendance> history = attendanceRepository.findAllByWorkerOrderByDateDesc(worker);

        // 2. Entity List -> DTO List 변환
        return history.stream()
                .map(ClockInOutResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
    // ▼▼▼▼▼ [★ 2. 이 메서드 전체를 추가 ★] ▼▼▼▼▼
    /**
     * [신규] 근로자가 특정 출퇴근 기록에 대해 "이의제기"를 제출
     */
    @Transactional
    public void submitObjection(User user, Long attendanceId, ObjectionRequestDto dto) {


        // 1. 수정할 출퇴근 기록을 찾음
        Attendance record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new EntityNotFoundException("해당 출퇴근 기록을 찾을 수 없습니다. ID: " + attendanceId));

        // 2. [★보안★] 이 기록이 "현재 로그인한 근로자"의 기록이 맞는지 확인
        if (!record.getWorker().getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("본인의 출퇴근 기록에만 이의제기를 할 수 있습니다.");
        }

        // 3. [★핵심★] "이의제기 메시지 보관함"에 메시지를 저장
        record.setObjectionMessage(dto.getMessage());

        // 4. DB에 저장
        attendanceRepository.save(record);
    }
    // ▲▲▲▲▲ [★ 2. 여기까지 추가 ★] ▲▲▲▲▲

    /**
     * [신규 헬퍼] 두 GPS 좌표 간의 거리를 미터(m) 단위로 계산 (Haversine 공식)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // m를 km로 변환
    }


    //----------------------------------
    // attendanceRecord에 값 저장
    //----------------------------------
    public AttendanceRecord calculateRecord(Attendance attendance, Long unitPrice) {

        LocalDate date = attendance.getDate();
        LocalTime in = attendance.getClockInTime();
        LocalTime out = attendance.getClockOutTime();

        // 퇴근 시간이 출근보다 빠르면 다음날 퇴근으로 처리 (야간 상황)
        if (out.isBefore(in)) {
            out = out.plusHours(24);
        }

        // 기준 시간 구간
        LocalTime BASIC_START = LocalTime.of(6, 0);
        LocalTime BASIC_END = LocalTime.of(16, 0);

        LocalTime EXT_START = LocalTime.of(14, 0);
        LocalTime EXT_END = LocalTime.of(22, 0);

        LocalTime NIGHT_START = LocalTime.of(22, 0);
        LocalTime NIGHT_END = LocalTime.of(6, 0).plusHours(24); // 다음날 06:00

        LocalTime DAY_END = LocalTime.MIDNIGHT.plusHours(24); // 24:00

        // 실제 날짜 기반 시간 변환
        LocalDateTime workIn = LocalDateTime.of(date, in);
        LocalDateTime workOut = LocalDateTime.of(date, out);

        // 구간 계산 함수
        double basic = getOverlapHours(workIn, workOut,
                LocalDateTime.of(date, BASIC_START), LocalDateTime.of(date, BASIC_END));

        double extended = getOverlapHours(workIn, workOut,
                LocalDateTime.of(date, EXT_START), LocalDateTime.of(date, EXT_END));

        double night = getOverlapHours(workIn, workOut,
                LocalDateTime.of(date, NIGHT_START),
                LocalDateTime.of(date.plusDays(1), NIGHT_END));

        // 초과근로: 00:00 ~ 06:00 중 야간이 아닌 근로 시간
        double overtime = 0;
        if (!in.isAfter(LocalTime.of(6, 0))) {
            overtime = getOverlapHours(workIn, workOut,
                    LocalDateTime.of(date, LocalTime.MIDNIGHT),
                    LocalDateTime.of(date, LocalTime.of(6, 0)));
        }

        // 총근로
        double total = (double) java.time.Duration.between(workIn, workOut).toMinutes() / 60.0;

        // 휴일 판단
        boolean isHoliday = date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;

        double holidayWork = isHoliday ? total : 0;

        double manHour = total / 8.0;

        return AttendanceRecord.builder()
                .worker(attendance.getWorker())
                .workDate(date)
                .actualTotalWork(round(total))
                .actualBasicWork(round(basic))
                .actualExtendedWork(round(extended))
                .actualOvertimeWork(round(overtime))
                .actualNightWork(round(night))
                .actualHolidayWork(round(holidayWork))
                .unitPrice(unitPrice)
                .manHour(round(manHour))
                .build();
    }

    private double getOverlapHours(LocalDateTime start1, LocalDateTime end1,
                                   LocalDateTime start2, LocalDateTime end2) {

        LocalDateTime start = start1.isAfter(start2) ? start1 : start2;
        LocalDateTime end = end1.isBefore(end2) ? end1 : end2;

        if (end.isBefore(start)) return 0;

        return Duration.between(start, end).toMinutes() / 60.0;
    }

    private double round(double value) {
        return Math.round(value * 100) / 100.0; // 소수점 2자리
    }
}
