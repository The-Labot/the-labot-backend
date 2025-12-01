package com.example.the_labot_backend.workers.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.repository.FileRepository;
import com.example.the_labot_backend.files.service.FileService;
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

    private final FileService fileService; // [★] 우리가 만든 파일 서비스 사용

    // 기존 로컬 저장 경로 유지
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // 1. 마이페이지 정보 조회
    @Transactional(readOnly = true)
    public WorkerMyPageResponse getMyPageInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        Worker worker = user.getWorker();
        if (worker == null) {
            throw new RuntimeException("해당 계정은 근로자 정보가 등록되지 않았습니다.");
        }

        // [★ 수정됨] FileService를 통해 S3 URL이 포함된 DTO 리스트 조회
        // 1) 근로계약서 (WORKER_CONTRACT) - 1개만 가져옴
        List<FileResponse> contracts = fileService.getFilesResponseByTarget("WORKER_CONTRACT", worker.getId());
        FileResponse contractFile = contracts.isEmpty() ? null : contracts.get(0);

        // 2) 급여명세서 (WORKER_PAYROLL) - 전체 리스트
        List<FileResponse> payrolls = fileService.getFilesResponseByTarget("WORKER_PAYROLL", worker.getId());

        // 3) 자격증 (WORKER_LICENSE) - 전체 리스트
        List<FileResponse> licenses = fileService.getFilesResponseByTarget("WORKER_LICENSE", worker.getId());

        // DTO 변환 후 반환
        return WorkerMyPageResponse.from(user, worker, contractFile, payrolls, licenses);
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


}
