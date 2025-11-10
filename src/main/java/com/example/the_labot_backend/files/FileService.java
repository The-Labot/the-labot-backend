package com.example.the_labot_backend.files;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    // 저장 폴더
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // 파일 저장
    public List<File> saveFiles(List<MultipartFile> multipartFiles, String targetType, Long targetId) {
        List<File> savedFiles = new ArrayList<>();

        //파일이 없으면 종료
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return savedFiles;
        }

        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.isEmpty()) continue;

            // 사용자가 업로드한 원래 이름
            String originalName = multipartFile.getOriginalFilename();

            // 서버에 저장할 때 이름 중복을 피하려고 UUID를 붙임
            String storedName = UUID.randomUUID() + "_" + originalName;

            // 
            Path savePath = Paths.get(UPLOAD_DIR, storedName);

            try {
                // 경로에 폴더가 없으면 자동 생성
                Files.createDirectories(savePath.getParent());

                // 업로드된 파일 데이터를 실제 파일로 복사
                multipartFile.transferTo(savePath.toFile());

                // File 엔티티 생성
                // 파일 메타 정보만 저장
                File fileEntity = File.builder()
                        .originalFileName(originalName)
                        .storedFileName(storedName)
                        .fileUrl("/uploads/" + storedName)
                        .contentType(multipartFile.getContentType())
                        .size(multipartFile.getSize())
                        .targetType(targetType)
                        .targetId(targetId)
                        .build();

                // DB에 저장 및 리스트에 추가
                savedFiles.add(fileRepository.save(fileEntity));

            } catch (IOException e) {
                throw new RuntimeException("파일 저장 실패: " + originalName, e);
            }
        }
        return savedFiles; // 저장된 파일 리스트
    }

    // 파일 삭제
    public void deleteFile(Long fileId) {
        fileRepository.deleteById(fileId);
    }

    // 타입과 Id를 가지고 파일찾기
    public List<File> getFilesByTarget(String targetType, Long targetId) {
        return fileRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }
}
