package com.example.the_labot_backend.sites.service;

import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.sites.dto.*;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.entity.embeddable.SiteSocialIns;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.the_labot_backend.authuser.entity.Role.ROLE_MANAGER;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

    /**
     * 현장 등록
     * @param request 등록 폼 데이터
     * @return 등록된 현장 ID
     */
    @Transactional
    public Long createSite(SiteCreateRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        // 1. 본사 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.: " + userId));

        // SocialIns 생성
        SiteSocialIns socialIns = createSocialIns(request.getSocialIns());

        // 2. 엔티티 빌드 (DTO -> Entity)
        Site site = Site.builder()
                .headOffice(user.getHeadOffice())
                .projectName(request.getProjectName())
                .contractType(request.getContractType())
                .siteManagerName(request.getSiteManagerName())
                .contractAmount(request.getContractAmount())
                .clientName(request.getClientName())
                .primeContractorName(request.getPrimeContractorName())

                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())

                .contractDate(request.getContractDate())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())

                .laborCostAccount(request.toBankAccount()) // 계좌 정보 변환

                .insuranceResponsibility(request.getInsuranceResponsibility())
                .employmentInsuranceSiteNum(request.getEmploymentInsuranceSiteNum())
                .primeContractorMgmtNum(request.getPrimeContractorMgmtNum())
                .isKisconReportTarget(request.isKisconReportTarget())

                // 핵심: 사회보험 정보 변환하여 주입
                .socialIns(socialIns)

                .build();

        // 3. 저장
        siteRepository.save(site);

        return site.getId();
    }

    // 현장 대시보드 조회
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(dashboard): " + userId));

        Long headOfficeId = user.getHeadOffice().getId();

        // 본사의 현장 리스트
        List<Site> siteList = siteRepository.findAllByHeadOffice_Id(headOfficeId);

        // 본사의 현장 수
        int totalSiteCount = siteList.size();

        // 본사 전체 근로자 수
        int activeWorkerCount = siteList.stream()
                .mapToInt(site -> userRepository.countBySite_IdAndRole(site.getId(), Role.ROLE_WORKER))
                .sum();

        // 현장별 목록 생성
        List<SiteListResponse> sites = siteList.stream()
                .map(site -> {

                    // 관리자 수
                    int managerCount = userRepository.countBySite_IdAndRole(site.getId(), ROLE_MANAGER);

                    // 근로자 수
                    int workerCount = userRepository.countBySite_IdAndRole(site.getId(), Role.ROLE_WORKER);

                    return SiteListResponse.builder()
                            .siteId(site.getId())
                            .siteName(site.getProjectName())
                            .siteAddress(site.getAddress())
                            .managerCount(managerCount)
                            .workerCount(workerCount)
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

//    // siteId로 현장 상세 조회
//    public SiteResponse getSiteBySiteId(Long siteId) {
//        Site site = siteRepository.findById(siteId)
//                .orElseThrow(() -> new IllegalArgumentException("현장을 찾을 수 없습니다. ID: " + siteId));
//        return toResponse(site);
//    }
//
//    // userId로 현장 상세 조회 (현장관리자)
//    public SiteResponse getSiteDetail(Long userId) {
//
//        // 1) 유저 → 본사 조회
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId:" + userId));
//
//        Site site = user.getSite();
//
//        return toResponse(site);
//    }
//
    // siteId로 현장 상세 조회 (본사관리자)
    @Transactional(readOnly = true)
    public SiteDetailResponse getSiteDetail(Long userId, Long siteId) {

        // 1) 유저 → 본사 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId:" + userId));
        Long headOfficeId = user.getHeadOffice().getId();

        // 2) 해당 본사의 현장인지 확인
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new NotFoundException("현장을 찾을 수 없습니다. siteId:" + siteId));

        if (!site.getHeadOffice().getId().equals(headOfficeId)) {
            throw new ForbiddenException("해당 현장은 당신의 본사 소속이 아닙니다. 접근 불가");
        }

        return SiteDetailResponse.from(site);
    }

    // userId로 현장 상세 조회 (현장관리자)
    @Transactional(readOnly = true)
    public SiteDetailResponse getSiteDetail(Long userId) {
        // 1) 유저 → 본사 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId:" + userId));

        if (user.getRole() != ROLE_MANAGER) {
            throw new ForbiddenException("현장 관리자만 현장 상세정보를 조회할 수 있습니다.");
        }


        Site site = user.getSite();
        if (site == null) {
            throw new NotFoundException("해당 사용자에게 배정된 현장이 없습니다.");
        }
        System.out.println(userId);
        System.out.println(site.getProjectName());
        return SiteDetailResponse.from(site);
    }

    // 현장 수정
    @Transactional
    public SiteDetailResponse updateSite(Long siteId, SiteUpdateRequest req) {

        // 1) 로그인 사용자 ID 가져오기 (SecurityContextHolder)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        // 2) 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));

        // 3) 수정할 Site 조회
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new NotFoundException("현장을 찾을 수 없습니다. id=" + siteId));

        // 4) 권한 & 소속 검증
        switch (user.getRole()) {
            // 본사관리자 → 본인 본사의 현장만 수정 가능
            case ROLE_ADMIN-> {
                if (user.getHeadOffice() == null ||
                        !user.getHeadOffice().getId().equals(site.getHeadOffice().getId())) {
                    throw new ForbiddenException("해당 본사의 현장만 수정할 수 있습니다.");
                }
            }
            // 현장관리자 → 자신에게 배정된 현장만 수정 가능
            case ROLE_MANAGER -> {
                if (user.getSite() == null || !user.getSite().getId().equals(siteId)) {
                    throw new ForbiddenException("배정된 현장만 수정할 수 있습니다.");
                }
            }
            default -> throw new ForbiddenException("현장을 수정할 권한이 없습니다.");
        }

        // -------------------------
        // 기본 필드 수정
        // -------------------------

        if (req.getProjectName() != null) site.setProjectName(req.getProjectName());
        if (req.getContractType() != null) site.setContractType(req.getContractType());
        if (req.getSiteManagerName() != null) site.setSiteManagerName(req.getSiteManagerName());
        if (req.getContractAmount() != null) site.setContractAmount(req.getContractAmount());
        if (req.getClientName() != null) site.setClientName(req.getClientName());
        if (req.getPrimeContractorName() != null) site.setPrimeContractorName(req.getPrimeContractorName());

        // 위치
        if (req.getAddress() != null) site.setAddress(req.getAddress());
        if (req.getLatitude() != null) site.setLatitude(req.getLatitude());
        if (req.getLongitude() != null) site.setLongitude(req.getLongitude());

        // 기간
        if (req.getContractDate() != null) site.setContractDate(req.getContractDate());
        if (req.getStartDate() != null) site.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) site.setEndDate(req.getEndDate());

        // 노무비 계좌
        if (req.getLaborCostBankName() != null)
            site.getLaborCostAccount().setBankName(req.getLaborCostBankName());

        if (req.getLaborCostAccountNumber() != null)
            site.getLaborCostAccount().setAccountNumber(req.getLaborCostAccountNumber());

        if (req.getLaborCostAccountHolder() != null)
            site.getLaborCostAccount().setAccountHolder(req.getLaborCostAccountHolder());

        // 행정정보
        if (req.getInsuranceResponsibility() != null)
            site.setInsuranceResponsibility(req.getInsuranceResponsibility());

        if (req.getEmploymentInsuranceSiteNum() != null)
            site.setEmploymentInsuranceSiteNum(req.getEmploymentInsuranceSiteNum());

        if (req.getPrimeContractorMgmtNum() != null)
            site.setPrimeContractorMgmtNum(req.getPrimeContractorMgmtNum());

        if (req.getKisconReportTarget() != null)
            site.setKisconReportTarget(req.getKisconReportTarget());

        // -------------------------
        // Social Ins 수정
        // -------------------------
        if (req.getSocialIns() != null) {
            updateSocialIns(req.getSocialIns(), site.getSocialIns());
        }

        return SiteDetailResponse.from(site);
    }

    // 현장 삭제 (Hard Delete)
    @Transactional
    public void deleteSite(Long siteId) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다. id=" + siteId));

        siteRepository.delete(site); // 완전 삭제
    }


    // 보험 등록 메소드
    private SiteSocialIns createSocialIns(SiteSocialInsDto dto) {
        if (dto == null) return null;

        return SiteSocialIns.builder()

                // 1. 국민연금
                .pensionDailyBizSymbol(dto.getPensionDailyBizSymbol())
                .pensionDailyJoinDate(dto.getPensionDailyJoinDate())

                .pensionRegularBizSymbol(dto.getPensionRegularBizSymbol())
                .pensionRegularJoinDate(dto.getPensionRegularJoinDate())
                .pensionFee(zero(dto.getPensionFee()))
                .pensionPaid(zero(dto.getPensionPaid()))
                .pensionRate(calcRate(dto.getPensionFee(), dto.getPensionPaid()))

                // 2. 건강보험
                .healthDailyBizSymbol(dto.getHealthDailyBizSymbol())
                .healthDailyJoinDate(dto.getHealthDailyJoinDate())

                .healthRegularBizSymbol(dto.getHealthRegularBizSymbol())
                .healthRegularJoinDate(dto.getHealthRegularJoinDate())
                .healthFee(zero(dto.getHealthFee()))
                .healthPaid(zero(dto.getHealthPaid()))
                .healthRate(calcRate(dto.getHealthFee(), dto.getHealthPaid()))

                // 3. 고용보험
                .employDailyMgmtNum(dto.getEmployDailyMgmtNum())
                .employDailyJoinDate(dto.getEmployDailyJoinDate())

                .employRegularMgmtNum(dto.getEmployRegularMgmtNum())
                .employRegularJoinDate(dto.getEmployRegularJoinDate())
                .employFee(zero(dto.getEmployFee()))
                .employPaid(zero(dto.getEmployPaid()))
                .employRate(calcRate(dto.getEmployFee(), dto.getEmployPaid()))

                // 4. 산재보험
                .accidentDailyMgmtNum(dto.getAccidentDailyMgmtNum())
                .accidentDailyJoinDate(dto.getAccidentDailyJoinDate())

                .accidentRegularMgmtNum(dto.getAccidentRegularMgmtNum())
                .accidentRegularJoinDate(dto.getAccidentRegularJoinDate())
                .accidentFee(zero(dto.getAccidentFee()))
                .accidentPaid(zero(dto.getAccidentPaid()))
                .accidentRate(calcRate(dto.getAccidentFee(), dto.getAccidentPaid()))

                // 5. 퇴직공제
                .isSeveranceTarget(dto.getSeveranceTarget() != null ? dto.getSeveranceTarget() : false)
                .severanceType(dto.getSeveranceType())
                .severanceDeductionNum(dto.getSeveranceDeductionNum())
                .severanceJoinDate(dto.getSeveranceJoinDate())
                .dailyDeductionAmount(dto.getDailyDeductionAmount())
                .totalSeverancePaidAmount(zero(dto.getTotalSeverancePaidAmount()))
                .severancePaymentRate(calcRate(dto.getDailyDeductionAmount() != null ?
                                dto.getDailyDeductionAmount().longValue() : 0L,
                        dto.getTotalSeverancePaidAmount()))
                .build();
    }
    
    // 보험 수정 메소드
    private void updateSocialIns(SiteSocialInsDto dto, SiteSocialIns ins) {

        // -------------------------------
        // 국민연금 (Daily)
        // -------------------------------
        boolean pensionDailyChanged = false;

        if (dto.getPensionDailyBizSymbol() != null)
            ins.setPensionDailyBizSymbol(dto.getPensionDailyBizSymbol());

        if (dto.getPensionDailyJoinDate() != null)
            ins.setPensionDailyJoinDate(dto.getPensionDailyJoinDate());

        // -------------------------------
        // 국민연금 (Regular)
        // -------------------------------
        boolean pensionRegularChanged = false;

        if (dto.getPensionRegularBizSymbol() != null)
            ins.setPensionRegularBizSymbol(dto.getPensionRegularBizSymbol());

        if (dto.getPensionRegularJoinDate() != null)
            ins.setPensionRegularJoinDate(dto.getPensionRegularJoinDate());

        if (dto.getPensionFee() != null) {
            ins.setPensionFee(zero(dto.getPensionFee()));
            pensionRegularChanged = true;
        }

        if (dto.getPensionPaid() != null) {
            ins.setPensionPaid(zero(dto.getPensionPaid()));
            pensionRegularChanged = true;
        }

        if (pensionRegularChanged) {
            ins.setPensionRate(
                    calcRate(ins.getPensionFee(), ins.getPensionPaid())
            );
        }


        // -------------------------------
        // 건강보험 Daily
        // -------------------------------
        boolean healthDailyChanged = false;

        if (dto.getHealthDailyBizSymbol() != null)
            ins.setHealthDailyBizSymbol(dto.getHealthDailyBizSymbol());

        if (dto.getHealthDailyJoinDate() != null)
            ins.setHealthDailyJoinDate(dto.getHealthDailyJoinDate());

        // -------------------------------
        // 건강보험 Regular
        // -------------------------------
        boolean healthRegularChanged = false;

        if (dto.getHealthRegularBizSymbol() != null)
            ins.setHealthRegularBizSymbol(dto.getHealthRegularBizSymbol());

        if (dto.getHealthRegularJoinDate() != null)
            ins.setHealthRegularJoinDate(dto.getHealthRegularJoinDate());

        if (dto.getHealthFee() != null) {
            ins.setHealthFee(zero(dto.getHealthFee()));
            healthRegularChanged = true;
        }

        if (dto.getHealthPaid() != null) {
            ins.setHealthPaid(zero(dto.getHealthPaid()));
            healthRegularChanged = true;
        }

        if (healthRegularChanged) {
            ins.setHealthRate(
                    calcRate(ins.getHealthFee(), ins.getHealthPaid())
            );
        }


        // -------------------------------
        // 고용보험 Daily
        // -------------------------------
        boolean employDailyChanged = false;

        if (dto.getEmployDailyMgmtNum() != null)
            ins.setEmployDailyMgmtNum(dto.getEmployDailyMgmtNum());

        if (dto.getEmployDailyJoinDate() != null)
            ins.setEmployDailyJoinDate(dto.getEmployDailyJoinDate());


        // -------------------------------
        // 고용보험 Regular
        // -------------------------------
        boolean employRegularChanged = false;

        if (dto.getEmployRegularMgmtNum() != null)
            ins.setEmployRegularMgmtNum(dto.getEmployRegularMgmtNum());

        if (dto.getEmployRegularJoinDate() != null)
            ins.setEmployRegularJoinDate(dto.getEmployRegularJoinDate());

        if (dto.getEmployFee() != null) {
            ins.setEmployFee(zero(dto.getEmployFee()));
            employRegularChanged = true;
        }

        if (dto.getEmployPaid() != null) {
            ins.setEmployPaid(zero(dto.getEmployPaid()));
            employRegularChanged = true;
        }

        if (employRegularChanged) {
            ins.setEmployRate(
                    calcRate(ins.getEmployFee(), ins.getEmployPaid())
            );
        }


        // -------------------------------
        // 산재보험 Daily
        // -------------------------------
        boolean accidentDailyChanged = false;

        if (dto.getAccidentDailyMgmtNum() != null)
            ins.setAccidentDailyMgmtNum(dto.getAccidentDailyMgmtNum());

        if (dto.getAccidentDailyJoinDate() != null)
            ins.setAccidentDailyJoinDate(dto.getAccidentDailyJoinDate());

        // -------------------------------
        // 산재보험 Regular
        // -------------------------------
        boolean accidentRegularChanged = false;

        if (dto.getAccidentRegularMgmtNum() != null)
            ins.setAccidentRegularMgmtNum(dto.getAccidentRegularMgmtNum());

        if (dto.getAccidentRegularJoinDate() != null)
            ins.setAccidentRegularJoinDate(dto.getAccidentRegularJoinDate());

        if (dto.getAccidentFee() != null) {
            ins.setAccidentFee(zero(dto.getAccidentFee()));
            accidentRegularChanged = true;
        }

        if (dto.getAccidentPaid() != null) {
            ins.setAccidentPaid(zero(dto.getAccidentPaid()));
            accidentRegularChanged = true;
        }

        if (accidentRegularChanged) {
            ins.setAccidentRate(
                    calcRate(ins.getAccidentFee(), ins.getAccidentPaid())
            );
        }


        // -------------------------------
        // 퇴직공제
        // -------------------------------
        if (dto.getSeveranceTarget() != null)
            ins.setSeveranceTarget(dto.getSeveranceTarget());

        if(dto.getSeveranceType() != null)
            ins.setSeveranceType(dto.getSeveranceType());

        if (dto.getSeveranceDeductionNum() != null)
            ins.setSeveranceDeductionNum(dto.getSeveranceDeductionNum());

        if (dto.getSeveranceJoinDate() != null)
            ins.setSeveranceJoinDate(dto.getSeveranceJoinDate());

        if (dto.getDailyDeductionAmount() != null)
            ins.setDailyDeductionAmount(zero(dto.getDailyDeductionAmount()));

        boolean severanceChanged = false;

        if (dto.getTotalSeverancePaidAmount() != null) {
            ins.setTotalSeverancePaidAmount(zero(dto.getTotalSeverancePaidAmount()));
            severanceChanged = true;
        }

        if (severanceChanged) {
            Long daily = ins.getDailyDeductionAmount() != null ?
                    ins.getDailyDeductionAmount().longValue() : 0L;
            ins.setSeverancePaymentRate(
                    calcRate(daily, ins.getTotalSeverancePaidAmount())
            );
        }
    }

    // 계산 관련 메소드들
    
    private Long zero(Long v) {
        return v == null ? 0L : v;
    }

    private Double calcRate(Long fee, Long paid) {
        if (fee == null || fee == 0) return 0.0;
        if (paid == null) return 0.0;
        return paid / (double) fee;
    }

    private Integer zero(Integer v) {
        return v == null ? 0 : v;
    }

}
