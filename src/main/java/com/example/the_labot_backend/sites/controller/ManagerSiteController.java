package com.example.the_labot_backend.sites.controller;

import com.example.the_labot_backend.sites.dto.SiteResponse;
import com.example.the_labot_backend.sites.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/sites")
@RequiredArgsConstructor
public class ManagerSiteController {

    private final SiteService siteService;

    //본인 현장 조회
    @GetMapping
    public ResponseEntity<?> getSite() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long id = Long.parseLong(auth.getName());

        SiteResponse response = siteService.getSiteByUserId(id);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "공지사항이 등록되었습니다.",
                "data", response
        ));
    }
}
