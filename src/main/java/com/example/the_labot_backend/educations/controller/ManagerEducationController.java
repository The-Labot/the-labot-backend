package com.example.the_labot_backend.educations.controller;

import com.example.the_labot_backend.educations.dto.EducationListResponse;
import com.example.the_labot_backend.educations.dto.EducationRequest;
import com.example.the_labot_backend.educations.dto.EducationResponse;
import com.example.the_labot_backend.educations.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/manager/educations")
@RequiredArgsConstructor
public class ManagerEducationController {

    private final EducationService educationService;

    // 안전교육일지 등록
    @PostMapping
    public ResponseEntity<?> createEducation(@RequestBody EducationRequest request) {

        Long userId = Long.parseLong(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        EducationResponse response = educationService.createEducation(userId, request);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "안전교육일지가 등록되었습니다.",
                "data", response
        ));
    }

    // 안전교육일지 목록 조회
    @GetMapping
    public ResponseEntity<?> getEducationList() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        List<EducationListResponse> response = educationService.getEducationByUser(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "data", response
        ));
    }

    // 안전교육일지 상세 조회
    @GetMapping("/{eduId}")
    public ResponseEntity<?> getEducation(@PathVariable Long eduId) {

        EducationResponse response = educationService.getEducationDetail(eduId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "data", response
        ));
    }

    // 안전교육일지 수정
    @PutMapping("/{eduId}")
    public ResponseEntity<?> updateEducation(
            @PathVariable Long eduId,
            @RequestBody EducationRequest request
    ) {
        EducationResponse response = educationService.updateEducation(eduId, request);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "안전교육일지가 수정되었습니다.",
                "data ", response
        ));
    }

    // 안전교육일지 삭제
    @DeleteMapping("/{eduId}")
    public ResponseEntity<?> delete(@PathVariable Long eduId) {

        educationService.delete(eduId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "안전교육일지가 삭제되었습니다."
        ));
    }
    
    // 안전교육일지 서명 pdf 추가

}