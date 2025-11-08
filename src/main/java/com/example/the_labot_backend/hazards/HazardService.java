package com.example.the_labot_backend.hazards;


import com.example.the_labot_backend.hazards.dto.HazardCreateRequest;
import com.example.the_labot_backend.hazards.dto.HazardDetailResponse;
import com.example.the_labot_backend.hazards.dto.HazardListResponse;
import com.example.the_labot_backend.hazards.entity.Hazard;
import com.example.the_labot_backend.hazards.entity.HazardStatus;
import com.example.the_labot_backend.users.User;
import com.example.the_labot_backend.users.UserRepository;
import com.example.the_labot_backend.users.dto.HazardStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;

import static com.example.the_labot_backend.global.util.TimeUtils.formatTimeAgo;

@Service
@RequiredArgsConstructor
public class HazardService {

    private final HazardRepository hazardRepository;
    private final UserRepository userRepository;

    // 신고 등록 (현장근로자)
    // @Transactional
    public void createHazard(Long reporterId, HazardCreateRequest request) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(HazardService) Id: " + reporterId));

        Hazard hazard = Hazard.builder()
                .hazardType(request.getHazardType())
                .description(request.getDescription())
                .location(request.getLocation())
                .fileUrl(request.getFileUrl())
                .urgent(request.isUrgent())
                .status(HazardStatus.WAITING)
                .reporter(reporter)
                .reportedAt(LocalDateTime.now())
                .build();

        hazardRepository.save(hazard);
    }

    // 목록 조회 (현장관리자)
    public List<HazardListResponse> getHazardList() {
        List<Hazard> reports = hazardRepository.findAll();

        return reports.stream()
                .map(hazard -> HazardListResponse.builder()
                        .id(hazard.getId())
                        .hazardType(hazard.getHazardType())
                        .reporter(hazard.getReporter().getName())
                        .location(hazard.getLocation())
                        .isUrgent(hazard.isUrgent())
                        .status(hazard.getStatus().name())
                        .reportedAt(formatTimeAgo(hazard.getReportedAt()))
                        .build())
                .toList();
    }

    // 상세 조회 (현장관리자)
    public HazardDetailResponse getHazardDetail(Long hazardId) {
        Hazard hazard = hazardRepository.findById(hazardId)
                .orElseThrow(() -> new RuntimeException("해당 위험요소 신고를 찾을 수 없습니다."));

        return HazardDetailResponse.builder()
                .id(hazard.getId())
                .hazardType(hazard.getHazardType())
                .reporter(hazard.getReporter().getName())
                .location(hazard.getLocation())
                .description(hazard.getDescription())
                .fileUrl(hazard.getFileUrl())
                .isUrgent(hazard.isUrgent())
                .status(hazard.getStatus().name())
                .reportedAt(hazard.getReportedAt()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .build();
    }

    // 상태 수정
    public Hazard updateStatus(Long id, HazardStatus newStatus) {
        Hazard hazard = hazardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 위험요소를 찾을 수 없습니다. id=" + id));

        hazard = Hazard.builder()
                .id(hazard.getId())
                .hazardType(hazard.getHazardType())
                .location(hazard.getLocation())
                .description(hazard.getDescription())
                .fileUrl(hazard.getFileUrl())
                .urgent(hazard.isUrgent())
                .status(newStatus)
                .reportedAt(hazard.getReportedAt())
                .reporter(hazard.getReporter())
                .build();

        return hazardRepository.save(hazard);
    }

    // 삭제
    public void deleteHazard(Long id) {
        if (!hazardRepository.existsById(id)) {
            throw new NoSuchElementException("해당 위험요소를 찾을 수 없습니다. id=" + id);
        }
        hazardRepository.deleteById(id);
    }
}
