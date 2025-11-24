package com.example.the_labot_backend.hazards.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.hazards.dto.HazardDetailResponse;
import com.example.the_labot_backend.hazards.dto.HazardListResponse;
import com.example.the_labot_backend.hazards.entity.Hazard;
import com.example.the_labot_backend.hazards.entity.HazardStatus;
import com.example.the_labot_backend.hazards.repository.HazardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

import static com.example.the_labot_backend.global.util.TimeUtils.formatTimeAgo;

@Service
@RequiredArgsConstructor
public class HazardService {

    private final HazardRepository hazardRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    // 위험요소 신고 등록 (현장근로자)
    @Transactional
    public void createHazard(String hazardType,
                             String location,
                             String description,
                             boolean urgent,
                             HazardStatus status,
                             List<MultipartFile> files,
                             Long userId)   {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(createHazard) userId:" + userId));

        // 신고 엔티티 생성
        Hazard hazard = Hazard.builder()
                .hazardType(hazardType)
                .location(location)
                .description(description)
                .urgent(urgent)
                .status(status)
                .reporter(reporter)
                .site(reporter.getSite()) // User가 Site에 속해있을 경우 자동 연결
                .build();

        hazardRepository.save(hazard);

        fileService.saveFiles(files, "HAZARD", hazard.getId());
    }

    // userId를 통한 위험요소 신고 목록 조회
    @Transactional(readOnly = true)
    public List<HazardListResponse> getHazardsByUser(Long userId) {

        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getHazardsByUser) userId:" + userId));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        // 여러개 조회, siteId를 조건으로 조회, 정렬, 정렬기준 Pinned, 내림차순
        List<Hazard> reports = hazardRepository.findAllBySite_Id(siteId);

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

    // 위험요소 신고 상세 조회
    @Transactional(readOnly = true)
    public HazardDetailResponse getHazardDetail(Long hazardId) {
        Hazard hazard = hazardRepository.findById(hazardId)
                .orElseThrow(() -> new RuntimeException("해당 위험요소 신고를 찾을 수 없습니다.(getHazardDetail) hazardId:" + hazardId));

        // 파일 조회
        List<File> files = fileService.getFilesByTarget("HAZARD", hazardId);

        return new HazardDetailResponse(hazard,files);
    }

    // 위험요소 신고 상태 수정
    @Transactional
    public Hazard updateStatus(Long hazardId, HazardStatus newStatus) {
        Hazard hazard = hazardRepository.findById(hazardId)
                .orElseThrow(() -> new NoSuchElementException("해당 위험요소 신고를 찾을 수 없습니다.(updateStatus) hazardId:" + hazardId));

        hazard = Hazard.builder()
                .id(hazard.getId())
                .hazardType(hazard.getHazardType())
                .location(hazard.getLocation())
                .description(hazard.getDescription())
                .urgent(hazard.isUrgent())
                .status(newStatus)
                .reportedAt(hazard.getReportedAt())
                .reporter(hazard.getReporter())
                .build();

        return hazardRepository.save(hazard);
    }

    // 위험요소 신고 삭제
    @Transactional
    public void deleteHazard(Long hazardId) {
        Hazard hazard = hazardRepository.findById(hazardId)
                .orElseThrow(()-> new RuntimeException("해당 위험요소 신고를 찾을 수 없습니다.(deleteHazard) hazardId:" + hazardId));

        // 위험요소 신고에 연결된 파일 삭제
        fileService.deleteFilesByTarget("HAZARD", hazardId);

        // 위험요소 신고 삭제
        hazardRepository.deleteById(hazardId);
    }
}
