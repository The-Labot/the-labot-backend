package com.example.the_labot_backend.notices.dto;

import com.example.the_labot_backend.notices.entity.NoticeCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeUpdateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String fileUrl;

    private NoticeCategory category;

    private boolean urgent;
    private boolean pinned;
}
