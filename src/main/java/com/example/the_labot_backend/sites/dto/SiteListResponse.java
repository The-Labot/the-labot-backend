package com.example.the_labot_backend.sites.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class SiteListResponse {
    private Long id;
    private String siteName;
//    private String siteAddress;
//    private String description;
//    private LocalDate startDate;
//    private LocalDate endDate;
//    private String constructionType;
//    private String client;
//    private String contractor;
}
