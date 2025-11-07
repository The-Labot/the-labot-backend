package com.example.the_labot_backend.worker;

import com.example.the_labot_backend.enums.WorkerStatus;
import com.example.the_labot_backend.users.UserRepository;
import com.example.the_labot_backend.worker.dto.WorkerDetailResponse;
import com.example.the_labot_backend.worker.dto.WorkerListResponse;
import com.example.the_labot_backend.worker.dto.WorkerUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;

    // 근로자 목록 조회
    public List<WorkerListResponse> getWorkers() {
        List<Worker> workers = workerRepository.findAll();

        // stream API
        // 리스트를 흐름으로 바꿔서 데이터를 반복문처럼 다루되, 선언형으로 쓸 수 있게 해줌
        return workers.stream()
                // map()은 리스트의 각 원소를 다른 형태로 바꿔주는 함수
                // worker를 WorkerListResponse로 변환하는 작업
                // 빌더 패턴
                // 객체를 만드는 방법을 편하게
                // 인자가 많을 때 가독성 좋게 쓸 수 있음
                // 마지막에 .build()를 호출하면 객체가 완성됨.
                .map(worker -> WorkerListResponse.builder()
                        .id(worker.getId())
                        .name(worker.getUser().getName())
                        .profileImage(worker.getProfileImage())
                        .position(worker.getPosition())
                        .status(worker.getStatus())
                        .build())
                // 스트림을 다시 리스트로 변환
                .toList();
    }

    // 근로자 상세 조회
    // 추후 출퇴근기록 등 기능 추가
    public WorkerDetailResponse getWorkerDetail(Long workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("해당 근로자를 찾을 수 없습니다."));

        return WorkerDetailResponse.builder()
                .id(worker.getId())
                .name(worker.getUser().getName())
                .phone(worker.getUser().getPhoneNumber())
                .address(worker.getAddress())
                .birthDate(worker.getBirthDate())
                .gender(worker.getGender())
                .nationality(worker.getNationality())
                .position(worker.getPosition())
                .site(worker.getUser().getSite())
                .build();
    }

    // 근로자 정보 수정 
    public void updateWorker(Long id, WorkerUpdateRequest dto) {
        Worker worker = workerRepository.findById(id)
                // 해당하는 id가 없을경우 예외를 던짐
                .orElseThrow(() -> new EntityNotFoundException("근로자를 찾을 수 없습니다."));
        worker.setAddress(dto.getAddress());
        worker.setPosition(dto.getPosition());
        worker.setStatus(dto.getStatus());
        workerRepository.save(worker);
    }
}
