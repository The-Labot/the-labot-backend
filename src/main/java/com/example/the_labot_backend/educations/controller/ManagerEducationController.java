package com.example.the_labot_backend.educations.controller;

import com.example.the_labot_backend.educations.dto.EducationCreateRequest;
import com.example.the_labot_backend.educations.dto.EducationDetailResponse;
import com.example.the_labot_backend.educations.dto.EducationListResponse;
import com.example.the_labot_backend.educations.service.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/educations")
@RequiredArgsConstructor
public class ManagerEducationController {

    private final EducationService educationService;

    // 안전교육일지 등록
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createEducation(
            @ModelAttribute EducationCreateRequest request,
            @RequestPart(value = "materials", required = false) List<MultipartFile> materials,   // 교육자료 파일
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,         // 교육 결과 사진
            @RequestPart(value = "signatures", required = false) List<MultipartFile> signatures  // 서명 PDF) {
    ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        educationService.createEducation(
                userId,
                request,
                materials,
                photos,
                signatures
        );

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "안전교육일지가 등록되었습니다."
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
    @GetMapping("/{educationId}")
    public ResponseEntity<?> getEducation(@PathVariable Long educationId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        EducationDetailResponse response = educationService.getEducationDetail(userId, educationId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "data", response
        ));
    }

    @PutMapping(value = "/{educationId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateEducation(
            @PathVariable Long educationId,
            @ModelAttribute EducationCreateRequest request,
            @RequestPart(value = "materials", required = false) List<MultipartFile> materials,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos,
            @RequestPart(value = "signatures", required = false) List<MultipartFile> signatures
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        educationService.updateEducation(
                userId,
                educationId,
                request,
                materials,
                photos,
                signatures
        );

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "안전교육일지가 수정되었습니다."
        ));
    }

    // 안전교육일지 삭제
    @DeleteMapping("/{educationId}")
    public ResponseEntity<?> delete(@PathVariable Long educationId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        educationService.deleteEducation(userId,educationId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "안전교육일지가 삭제되었습니다."
        ));
    }
}