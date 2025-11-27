package com.example.the_labot_backend.files.controller;

import com.example.the_labot_backend.files.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    /**
     * 파일 새로 업로드 (작업일보/위험요소/공지사항 등 모든 곳에서 공통 사용)
     * POST /api/files/upload?targetType=REPORT&targetId=15
     */
    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Long targetId
    ) {

        fileService.deleteFilesByTarget(targetType,targetId);
        fileService.saveFiles(files, targetType, targetId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "파일 저장 성공"
        ));
    }

//    /**
//     * 파일 단건 삭제
//     */
//    @DeleteMapping("/{fileId}")
//    public ResponseEntity<?> deleteFile(@PathVariable Long fileId) {
//
//        fileService.deleteFilesByTarget(targetType,targetId);
//
//        return ResponseEntity.ok(Map.of(
//                "status", 200,
//                "message", "파일 삭제 성공"
//        ));
//    }

//    /**
//     * 특정 엔티티(targetType & targetId)에 연결된 파일 목록 조회
//     * GET /api/files?targetType=DAILY_REPORT&targetId=15
//     */
//    @GetMapping
//    public ResponseEntity<?> getFiles(
//            @RequestParam("targetType") String targetType,
//            @RequestParam("targetId") Long targetId
//    ) {
//        List<FileResponse> files = fileService.getFilesResponseByTarget(targetType, targetId);
//
//        return ResponseEntity.ok(
//                new Result("파일 조회 성공", 200, files)
//        );
//    }

    record Result(String message, int status, Object data) {}
}
