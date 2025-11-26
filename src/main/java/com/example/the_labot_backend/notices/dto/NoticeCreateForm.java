package com.example.the_labot_backend.notices.dto;

import com.example.the_labot_backend.notices.entity.NoticeCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeCreateForm {
    private String title;
    private String content;
    private NoticeCategory category;
    private boolean urgent;
    private boolean pinned;
}
