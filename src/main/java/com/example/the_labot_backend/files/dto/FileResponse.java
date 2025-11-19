package com.example.the_labot_backend.files.dto;

import com.example.the_labot_backend.files.entity.File;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FileResponse {
    private Long id;
    private String fileUrl;
    private String originalFileName;

    public static FileResponse from(File file) {
        return FileResponse.builder()
                .id(file.getId())
                .fileUrl(file.getFileUrl())
                .originalFileName(file.getOriginalFileName())
                .build();
    }

    public static List<FileResponse> fromList(List<File> files) {
        return files.stream()
                .map(FileResponse::from)
                .toList();
    }
}
