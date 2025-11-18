package com.example.the_labot_backend.sites.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.sites.dto.SiteListResponse;
import com.example.the_labot_backend.sites.dto.SiteRequest;
import com.example.the_labot_backend.sites.dto.SiteResponse;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

    // 현장 등록
    public void createSite(SiteRequest request) {
        Site site = Site.builder()
                .siteName(request.getSiteName())
                .siteAddress(request.getSiteAddress())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .constructionType(request.getConstructionType())
                .client(request.getClient())
                .contractor(request.getContractor())
                .build();

        Site saved = siteRepository.save(site);
    }

    // 전체 현장 조회 ( 응답 값 아직 미정 )
    public List<SiteListResponse> getAllSites() {

        List<Site> sites = siteRepository.findAll();
        return sites.stream()
                .map(site -> SiteListResponse.builder()
                        .id(site.getId())
                        .siteName(site.getSiteName())
                        .build())
                .toList();
    }

    // siteId로 현장 상세 조회
    public SiteResponse getSiteBySiteId(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("현장을 찾을 수 없습니다. ID: " + siteId));
        return toResponse(site);
    }

    // userId로 현장 상세 조회
    public SiteResponse getSiteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. ID: " + userId));

        Long siteId = user.getSite().getId();

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("현장을 찾을 수 없습니다. ID: " + siteId));
        return toResponse(site);
    }

    // 현장 수정
    public SiteResponse updateSite(Long siteId, SiteRequest request) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("현장을 찾을 수 없습니다. ID: " + siteId));

        site.setSiteName(request.getSiteName());
        site.setSiteAddress(request.getSiteAddress());
        site.setDescription(request.getDescription());
        site.setStartDate(request.getStartDate());
        site.setEndDate(request.getEndDate());
        site.setConstructionType(request.getConstructionType());
        site.setClient(request.getClient());
        site.setContractor(request.getContractor());

        Site updated = siteRepository.save(site);
        return toResponse(updated);
    }

    // 현장 삭제
    public void deleteSite(Long id) {
        siteRepository.deleteById(id);
    }

    // DTO 변환, 공통응답
    private SiteResponse toResponse(Site site) {
        return SiteResponse.builder()
                .id(site.getId())
                .siteName(site.getSiteName())
                .siteAddress(site.getSiteAddress())
                .description(site.getDescription())
                .startDate(site.getStartDate())
                .endDate(site.getEndDate())
                .constructionType(site.getConstructionType())
                .client(site.getClient())
                .contractor(site.getContractor())
                .build();
    }
}
