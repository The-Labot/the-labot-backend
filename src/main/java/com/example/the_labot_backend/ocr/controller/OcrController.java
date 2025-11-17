package com.example.the_labot_backend.ocr.controller;

import com.example.the_labot_backend.ocr.dto.ContractDataDto;

import com.example.the_labot_backend.ocr.dto.FinalSaveDto;
import com.example.the_labot_backend.ocr.dto.IdCardDataDto;
import com.example.the_labot_backend.ocr.service.ContractOcrService;
import com.example.the_labot_backend.ocr.service.IdCardOcrService;
import com.example.the_labot_backend.ocr.service.RegistrationService;
import com.example.the_labot_backend.workers.Worker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/register")
public class OcrController {
    private final ContractOcrService contractOcrService;
    private final IdCardOcrService idCardOcrService;
    private final RegistrationService registrationService; //

    public OcrController(ContractOcrService ocrService, IdCardOcrService idCardOcrService, RegistrationService registrationService) {
        this.contractOcrService = ocrService;
        this.idCardOcrService = idCardOcrService;
        this.registrationService = registrationService;
    }

    /**

     * 앱에서 사진 파일(file)을 이 API로 전송
     */
    @PostMapping("/upload-contract")
    public ResponseEntity<ContractDataDto> uploadContract(@RequestParam("file") MultipartFile file) {
        try {

            ContractDataDto extractedData = contractOcrService.processContract(file);
            return ResponseEntity.ok(extractedData);

        } catch (Exception e) {
            e.printStackTrace();
            // 실제로는 @ControllerAdvice 등으로 더 정교한 에러 처리가 필요
            return ResponseEntity.status(500).body(null);
        }
    }
    // vvvvvvvvvv ★ [신규] 신분증 API 엔드포인트 추가 vvvvvvvvvv
    /**
     * [★ 신규 6단계 ★]
     * 앱에서 "신분증" 사진 파일(file)을 이 API로 전송
     * (주민등록증, 운전면허증 모두 이 API 하나로 처리)
     */
    @PostMapping("/upload-id-card")
    public ResponseEntity<IdCardDataDto> uploadIdCard(@RequestParam("file") MultipartFile file) {
        try {
            IdCardDataDto extractedData = idCardOcrService.processIdCard(file);
            return ResponseEntity.ok(extractedData);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    // ^^^^^^^^^^ ★ [신규] 신분증 API 엔드포인트 추가 ^^^^^^^^^^

    /**
     * [순서 7] 앱에서 수정한 최종 데이터를 "메모리"에 저장
     * 앱에서 수정한 최종 데이터를 JSON(@RequestBody)으로 이 API에 전송
     */
    @PostMapping("/create-worker")
    public ResponseEntity<String> saveMember(@RequestBody FinalSaveDto finalData) {
        try {
            // (이제 MemberService는 FianlSaveDto를 받아서 모든 정보를 저장함)
            registrationService.saveNewMember(finalData);
            return ResponseEntity.ok("근로자 등록이 (메모리에) 완료되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * (테스트용) 현재 메모리에 저장된 모든 근로자 목록을 JSON으로 확인
     * 웹 브라우저나 Postman에서 GET http://localhost:8080/api/register/all-members
     */
    @GetMapping("/all-workers") // 1. 주소도 바꿨어
    public ResponseEntity<List<Worker>> getAllWorkers() { // 2. 반환 타입을 List<Worker>로 변경
        try {
            // 3. registrationService에 새로 만들 메서드를 호출
            List<Worker> workers = registrationService.getAllWorkers();
            return ResponseEntity.ok(workers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
