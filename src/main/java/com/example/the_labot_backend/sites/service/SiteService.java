package com.example.the_labot_backend.sites.service;

import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import com.example.the_labot_backend.sites.dto.DashboardResponse;
import com.example.the_labot_backend.sites.dto.SiteListResponse;
import com.example.the_labot_backend.sites.dto.SiteResponse;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    // 현장 등록
    @Transactional
    public void createSite(Long userId,
                           String siteName,
                           String siteAddress,
                           String description,
                           LocalDate startDate,
                           LocalDate endDate,
                           String constructionType,
                           String client,
                           String contractor,
                           Double latitude,
                           Double longitude,
                           List<MultipartFile> files) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(dashboard): " + userId));

        HeadOffice headOffice = user.getHeadOffice();

        Site site = Site.builder()
                .siteName(siteName)
                .siteAddress(siteAddress)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .constructionType(constructionType)
                .client(client)
                .contractor(contractor)
                .latitude(latitude)
                .longitude(longitude)
                .headOffice(headOffice)
                .build();

        siteRepository.save(site);

        // 파일 업로드 (로컬 or S3)
        fileService.saveFiles(files, "SITE_MAP", site.getId());
    }

    // 현장 대시보드 조회
    public DashboardResponse getDashboard(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(dashboard): " + userId));

        Long headOfficeId = user.getHeadOffice().getId();

        // 본사의 현장 리스트 
        List<Site> siteList = siteRepository.findAllByHeadOffice_Id(headOfficeId);
        
        // 본사의 현장 수
        int totalSiteCount = siteList.size();

        // 본사 전체 근로자 수
        int activeWorkerCount = userRepository.countByHeadOffice_IdAndRole(headOfficeId, Role.ROLE_WORKER);

        // 현장별 목록 생성
        List<SiteListResponse> sites = siteList.stream()
                .map(site -> {

                    // 관리자 수
                    int managerCount = userRepository.countBySite_IdAndRole(site.getId(), Role.ROLE_MANAGER);

                    // 근로자 수
                    int workerCount = userRepository.countBySite_IdAndRole(site.getId(), Role.ROLE_WORKER);

//                    // 최근 보고일자 (작업현황 기준) (미정)
//                    LocalDate lastReportDate = reportRepository
//                            .findTopBySite_IdOrderByCreatedAtDesc(site.getId())
//                            .map(r -> r.getCreatedAt().toLocalDate())
//                            .orElse(null);

                    return SiteListResponse.builder()
                            .siteId(site.getId())
                            .siteName(site.getSiteName())
                            .siteAddress(site.getSiteAddress())
                            .managerCount(managerCount)
                            .workerCount(workerCount)
//                            .lastReportDate(lastReportDate != null ? lastReportDate.toString() : null)
                            .build();
                })
                .toList();

        return DashboardResponse.builder()
                .totalSiteCount(totalSiteCount)
                .activeWorkerCount(activeWorkerCount)
                .siteList(sites)
                .build();
    }

//    // userId를 통해 해당 본사의 현장 목록 조회 (보류: 필요없음)
//    public List<SiteListResponse> getSitesByUser(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getSitesByUser) userId:" + userId));
//
//        // user로 headOfficeId 찾기
//        Long headOfficeId = user.getHeadOffice().getId();
//
//        List<Site> sites = siteRepository.findAllByHeadOffice_Id(headOfficeId);
//
//        return sites.stream()
//                .map(site -> SiteListResponse.builder()
//                        .siteName(site.getSiteName())
//                        .siteAddress(site.getSiteAddress())
//                        .managerCount(userRepository.countBySite_IdAndRole(site.getId(), Role.ROLE_MANAGER))
//                        .workerCount(userRepository.countBySite_IdAndRole(site.getId(), Role.ROLE_WORKER))
//                        .build())
//                .toList();
//    }

    // siteId로 현장 상세 조회
    public SiteResponse getSiteBySiteId(Long siteId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("현장을 찾을 수 없습니다. ID: " + siteId));
        return toResponse(site);
    }

    // userId로 현장 상세 조회 (현장관리자)
    public SiteResponse getSiteDetail(Long userId) {

        // 1) 유저 → 본사 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId:" + userId));

        Site site = user.getSite();

        return toResponse(site);
    }

    // siteId로 현장 상세 조회 (본사관리자)
    public SiteResponse getSiteDetail(Long userId, Long siteId) {

        // 1) 유저 → 본사 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId:" + userId));
        Long headOfficeId = user.getHeadOffice().getId();

        // 2) 해당 본사의 현장인지 확인
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다. siteId:" + siteId));

        if (!site.getHeadOffice().getId().equals(headOfficeId)) {
            throw new RuntimeException("해당 현장은 당신의 본사 소속이 아닙니다. 접근 불가");
        }

        return toResponse(site);
    }

    // 현장 수정
    @Transactional
    public SiteResponse updateSite(Long userId,
                                   Long siteId,
                                   String siteName,
                                   String siteAddress,
                                   String description,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   String constructionType,
                                   String client,
                                   String contractor,
                                   Double latitude,
                                   Double longitude,
                                   List<MultipartFile> files) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId:" + userId));

        Long headOfficeId = user.getHeadOffice().getId();

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다. siteId:" + siteId));

        if (!site.getHeadOffice().getId().equals(headOfficeId)) {
            throw new RuntimeException("해당 현장은 당신의 본사 소속이 아닙니다. 접근 불가");
        }

        site.setSiteName(siteName);
        site.setSiteAddress(siteAddress);
        site.setDescription(description);
        site.setStartDate(startDate);
        site.setEndDate(endDate);
        site.setConstructionType(constructionType);
        site.setClient(client);
        site.setContractor(contractor);
        site.setLatitude(latitude);
        site.setLongitude(longitude);

        Site updated = siteRepository.save(site);

        // 기존 파일 전체 삭제
        fileService.deleteFilesByTarget("SITE_MAP", site.getId());

        fileService.saveFiles(files, "SITE_MAP", site.getId());

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
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())
                .build();
    }
}
