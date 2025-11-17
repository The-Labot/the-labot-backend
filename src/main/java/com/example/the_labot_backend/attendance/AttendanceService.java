package com.example.the_labot_backend.attendance;

import com.example.the_labot_backend.attendance.dto.ClockInOutRequestDto;
import com.example.the_labot_backend.attendance.dto.ObjectionRequestDto;
import com.example.the_labot_backend.sites.Site;
import com.example.the_labot_backend.users.entity.User;
import com.example.the_labot_backend.workers.Worker;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.the_labot_backend.attendance.dto.ClockInOutResponseDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;


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
            if (!now.isBefore(TEN_AM)) { // 10시 00분 00초 "이후" (>= 10:00)
                status = AttendanceStatus.ABSENT; // 결근
            } else if (!now.isBefore(NINE_AM)) { // 9시 00분 00초 "이후" (>= 9:00 and < 10:00)
                status = AttendanceStatus.LATE; // 지각
            } else { // 9시 "이전" (< 9:00)
                status = AttendanceStatus.PRESENT; // 출근(정상)
            }
            // ▲▲▲ [★ 2. 10시/9시 기준 판별 로직 수정] ▲▲▲



            Attendance newRecord = Attendance.builder()
                    .worker(worker)
                    .date(today)
                    .clockInTime(now) // [★] 그냥 현재 시간 저장
                    .status(status)
                    .build();
            Attendance savedRecord = attendanceRepository.save(newRecord);
            return ClockInOutResponseDto.fromEntity(savedRecord);

        } else {
            // --- 4. [퇴근 처리] ---
            Attendance record = existingRecord.get();
            if (record.getClockOutTime() != null) {
                throw new IllegalStateException("이미 퇴근 처리가 완료되었습니다.");
            }
            record.setClockOutTime(now); // [★] 그냥 현재 시간 저장
            Attendance savedRecord = attendanceRepository.save(record);
            return ClockInOutResponseDto.fromEntity(savedRecord);
        }
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
}
