package com.example.the_labot_backend.worker;

import com.example.the_labot_backend.worker.dto.WorkerUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    // 근로자 목록 조회
    @GetMapping
    // ResponseEntity는 HTTP 응답 전체를 직접 구성할 수 있는 클래스
    // <?> -> 와일드카드: 아직 타입을 구체적으로 정하지 않음
    // Map을 반환하지만 나중에 String을 반환할 수 도 있음.
    public ResponseEntity<?> getAllWorkers() {
        // ok는 HTTP 200 OK 응답을 만들어주는 정적 메소드
        // Body에 Map을 담아서 보냄. Map을 반환하면 클라이언트에게 JSON으로 보냄.
        // Map.of(...): 불변 Map을 간단하게 만듬.

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "근로자 목록 조회 성공",
                "data", workerService.getWorkers()
        ));
        
        //실패 코드도 만들어야할듯
    }

    // 가비지 컬렉터
    // 객체의 생성주기에 대한 방식으로 메모리는 관리하는 방법
    // 일정 주기나 메모리가 부족하면 JVM이 판단해 참조가 되지 않는 객체들을 모두 없앰.
    // 익명 객체의 경우 참조가 없는 것 처럼 보임
    // 하지만 참조는 여전히 존재함. 단지 해당 문장이 실행되는 동안만 참조를 유지함.

    // 근로자 상세 조회
    @GetMapping("/{workerId}")
    public ResponseEntity<?> getWorkerDetail(@PathVariable Long workerId) {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "근로자 상세 정보 조회 성공",
                "data", workerService.getWorkerDetail(workerId)
        ));
    }
    // Map<String, Object> 타입의 객체를 만들어 반환
    // @RestController or @ResponseBody가 붙어 있으면 뷰가 아닌 JSON으로 응답해야 하는 데이터라는 것을 인지
    // 내부적으로 HttpMessageConverter중 하나인 Jackson(라이브러리 이름: jackson-databind)을 사용해서 Java객체를 JSON 문자열로 변환함.
    // 객체라면?
    // 객체안의 필드를 또 재귀적으로 반환한다.

    // 근로자 정보 수정
    @PutMapping("/{workerId}")
    public ResponseEntity<?> updateWorker(@PathVariable Long workerId,
                                          @RequestBody WorkerUpdateRequest dto) {
        workerService.updateWorker(workerId, dto);
        return ResponseEntity.ok(Map.of("status", 200, "message", "근로자 정보 수정 완료"));
    }
}

