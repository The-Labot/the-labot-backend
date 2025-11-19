package com.example.the_labot_backend.sites.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SiteListResponse {
    private Long siteId;
    private String siteName;
    private String siteAddress;
    private int managerCount;
    private int workerCount;
    // 최근 작업 일자
}
