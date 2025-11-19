package com.example.the_labot_backend.sites.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {
    private int totalSiteCount;
    private int activeWorkerCount;
    private List<SiteListResponse> siteList;
}
