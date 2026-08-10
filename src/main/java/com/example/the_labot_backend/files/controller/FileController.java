package com.example.the_labot_backend.files.controller;

import com.example.the_labot_backend.files.entity.FileTargetType;
import com.example.the_labot_backend.files.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 파일 새로 업로드 (작업일보/위험요소/공지사항 등 모든 곳에서 공통 사용)
     * POST /api/manager/files/save?targetType=REPORT&targetId=15
     */
    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("targetType") FileTargetType targetType,
            @RequestParam("targetId") Long targetId
    ) {

        fileService.deleteFilesByTarget(targetType, targetId);
        fileService.saveFiles(files, targetType, targetId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "파일 저장 성공"
        ));
    }
}
