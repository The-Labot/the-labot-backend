package com.example.the_labot_backend.sites.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SiteRequest {
    private String siteName;
    private String siteAddress;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String constructionType;
    private String client;
    private String contractor;
    private Double latitude;
    private Double longitude;
}
