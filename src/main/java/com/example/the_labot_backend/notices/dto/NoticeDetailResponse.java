package com.example.the_labot_backend.notices.dto;

import com.example.the_labot_backend.files.File;
import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class NoticeDetailResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final NoticeCategory category;
    private final boolean urgent;
    private final boolean pinned;
    private final LocalDateTime createdAt;
    private final List<FileResponse> files;

    public NoticeDetailResponse(Notice notice, List<File> files) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.category = notice.getCategory();
        this.urgent = notice.isUrgent();
        this.pinned = notice.isPinned();
        this.createdAt = notice.getCreatedAt();
        this.files = files.stream().map(FileResponse::new).toList();
    }

    @Getter
    static class FileResponse {
        private Long id;
        private String fileUrl;
        private String originalFileName;

        public FileResponse(File file) {
            this.id = file.getId();
            this.fileUrl = file.getFileUrl();
            this.originalFileName = file.getOriginalFileName();
        }
    }
}
