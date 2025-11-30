package com.example.the_labot_backend.workers.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.repository.FileRepository;
import com.example.the_labot_backend.workers.dto.WorkerMyPageResponse;
import com.example.the_labot_backend.workers.dto.WorkerMyPageUpdateRequest;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.embeddable.WorkerBankAccount;
import org.springframework.core.io.Resource;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerMyPageService {
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    // 기존 로컬 저장 경로 유지
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // 1. 마이페이지 정보 조회 로직
    @Transactional(readOnly = true)
    public WorkerMyPageResponse getMyPageInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        Worker worker = user.getWorker();
        if (worker == null) {
            throw new RuntimeException("해당 계정은 근로자 정보가 등록되지 않았습니다.");
        }

        // 파일 ID 조회 (헬퍼 메서드 활용)
        Long contractId = getFileIdByTypeAndTarget("WORKER_CONTRACT", worker.getId());
        Long payrollId = getFileIdByTypeAndTarget("WORKER_PAYROLL", worker.getId());
        Long certId = getFileIdByTypeAndTarget("WORKER_LICENSE", worker.getId());

        // DTO 변환 후 반환
        return WorkerMyPageResponse.from(user, worker, contractId, payrollId, certId);
    }

    // 2. [★ 신규 추가] 마이페이지 정보 수정
    @Transactional
    public void updateMyPageInfo(Long userId, WorkerMyPageUpdateRequest request) {

        // (1) User & Worker 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        Worker worker = user.getWorker();
        if (worker == null) {
            throw new RuntimeException("근로자 정보가 존재하지 않습니다.");
        }

        // (2) User 정보 수정 (전화번호)
        // 주의: 전화번호가 로그인 ID라면 중복 체크 로직이 필요할 수 있음
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // (3) Worker 정보 수정 (주소, 생년월일, 비상연락처)
        if (request.getAddress() != null) worker.setAddress(request.getAddress());
        if (request.getBirthDate() != null) worker.setBirthDate(request.getBirthDate());
        if (request.getEmergencyNumber() != null) worker.setEmergencyNumber(request.getEmergencyNumber());

        // (4) 계좌 정보 수정 (Embedded 타입 처리)
        // 계좌 정보는 무조건 있다고 가정하고 바로 가져와서 수정
        WorkerBankAccount account = worker.getBankAccount();

        if (request.getBankName() != null) account.setBankName(request.getBankName());
        if (request.getAccountNumber() != null) account.setAccountNumber(request.getAccountNumber());
        if (request.getAccountHolder() != null) account.setAccountHolder(request.getAccountHolder());

        // @Transactional 때문에 save 호출 안 해도 자동 저장됨
    }

    // 2. 파일 다운로드 로직 (로컬 파일 시스템 사용)
    @Transactional(readOnly = true)
    public FileDownloadDto downloadFile(Long userId, Long fileId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            Worker worker = user.getWorker();
            if (worker == null) throw new RuntimeException("근로자 정보 없음");

            // 파일 메타데이터 조회
            File fileEntity = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

            // ★ 권한 검증: 본인의 파일인지 확인
            if (!fileEntity.getTargetId().equals(worker.getId()) ||
                    !fileEntity.getTargetType().startsWith("WORKER")) {
                throw new RuntimeException("본인의 파일만 다운로드할 수 있습니다.");
            }

            // 실제 파일 리소스 로드 (로컬 경로 사용)
            Path filePath = Paths.get(UPLOAD_DIR + fileEntity.getStoredFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) throw new RuntimeException("디스크에 파일이 존재하지 않습니다.");

            // 컨트롤러에게 넘겨줄 데이터 포장
            return FileDownloadDto.builder()
                    .resource(resource)
                    .originalFileName(fileEntity.getOriginalFileName())
                    .contentType(fileEntity.getContentType() != null ? fileEntity.getContentType() : "application/octet-stream")
                    .build();

        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로 오류", e);
        }
    }

    // (내부 헬퍼 메서드) 파일 ID 찾기
    private Long getFileIdByTypeAndTarget(String type, Long targetId) {
        List<File> files = fileRepository.findByTargetTypeAndTargetId(type, targetId);
        return files.isEmpty() ? null : files.get(0).getId();
    }

    // (내부 DTO) 서비스 -> 컨트롤러 전달용
    @Getter
    @Builder
    public static class FileDownloadDto {
        private Resource resource;
        private String originalFileName;
        private String contentType;
    }
}
