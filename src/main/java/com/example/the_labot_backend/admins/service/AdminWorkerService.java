package com.example.the_labot_backend.admins.service;


import org.springframework.transaction.annotation.Transactional;
import com.example.the_labot_backend.admins.dto.AdminWorkerListResponse;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminWorkerService {
    private final WorkerRepository workerRepository;

    /**
     * 본사 관리자(adminUser)가 속한 본사의 모든 현장 근로자 조회
     */
    public List<AdminWorkerListResponse> getAllWorkersByHeadOffice(User adminUser) {
        // 1. 관리자의 본사 정보 확인
        if (adminUser.getHeadOffice() == null) {
            throw new RuntimeException("해당 관리자는 본사 소속이 아닙니다.");
        }

        Long headOfficeId = adminUser.getHeadOffice().getId();

        // 2. 해당 본사 ID로 모든 근로자 조회 (Repository 호출)
        List<Worker> workers = workerRepository.findAllByHeadOfficeId(headOfficeId);

        // 3. DTO 변환
        return workers.stream()
                .map(AdminWorkerListResponse::from)
                .collect(Collectors.toList());
    }
}
