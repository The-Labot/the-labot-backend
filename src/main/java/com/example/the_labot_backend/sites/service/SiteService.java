package com.example.the_labot_backend.sites.service;

import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.sites.dto.*;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.entity.embeddable.SiteSocialIns;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    /**
     * 현장 등록
     * @param userId 로그인한 본사관리자 userID
     * @param request 등록 폼 데이터
     * @return 등록된 현장 ID
     */
    @Transactional
    public Long createSite(Long userId, SiteCreateRequest request) {

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
    public SiteDetailResponse getSiteDetail(Long userId, Long siteId) {

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

        return SiteDetailResponse.from(site);
    }

    // siteId로 현장 상세 조회 (현장관리자)
    public SiteDetailResponse getSiteDetail(Long userId) {

        // 1) 유저 → 본사 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId:" + userId));
        Site site = user.getSite();

        return SiteDetailResponse.from(site);
    }

    // 현장 수정
    @Transactional
    public SiteDetailResponse updateSite(Long siteId, SiteUpdateRequest req) {

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다. id=" + siteId));

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
                .pensionDailyFee(zero(dto.getPensionDailyFee()))
                .pensionDailyPaid(zero(dto.getPensionDailyPaid()))
                .pensionDailyRate(calcRate(dto.getPensionDailyFee(), dto.getPensionDailyPaid()))

                .pensionRegularBizSymbol(dto.getPensionRegularBizSymbol())
                .pensionRegularJoinDate(dto.getPensionRegularJoinDate())
                .pensionRegularFee(zero(dto.getPensionRegularFee()))
                .pensionRegularPaid(zero(dto.getPensionRegularPaid()))
                .pensionRegularRate(calcRate(dto.getPensionRegularFee(), dto.getPensionRegularPaid()))

                // 2. 건강보험
                .healthDailyBizSymbol(dto.getHealthDailyBizSymbol())
                .healthDailyJoinDate(dto.getHealthDailyJoinDate())
                .healthDailyFee(zero(dto.getHealthDailyFee()))
                .healthDailyPaid(zero(dto.getHealthDailyPaid()))
                .healthDailyRate(calcRate(dto.getHealthDailyFee(), dto.getHealthDailyPaid()))

                .healthRegularBizSymbol(dto.getHealthRegularBizSymbol())
                .healthRegularJoinDate(dto.getHealthRegularJoinDate())
                .healthRegularFee(zero(dto.getHealthRegularFee()))
                .healthRegularPaid(zero(dto.getHealthRegularPaid()))
                .healthRegularRate(calcRate(dto.getHealthRegularFee(), dto.getHealthRegularPaid()))

                // 3. 고용보험
                .employDailyMgmtNum(dto.getEmployDailyMgmtNum())
                .employDailyJoinDate(dto.getEmployDailyJoinDate())
                .employDailyFee(zero(dto.getEmployDailyFee()))
                .employDailyPaid(zero(dto.getEmployDailyPaid()))
                .employDailyRate(calcRate(dto.getEmployDailyFee(), dto.getEmployDailyPaid()))

                .employRegularMgmtNum(dto.getEmployRegularMgmtNum())
                .employRegularJoinDate(dto.getEmployRegularJoinDate())
                .employRegularFee(zero(dto.getEmployRegularFee()))
                .employRegularPaid(zero(dto.getEmployRegularPaid()))
                .employRegularRate(calcRate(dto.getEmployRegularFee(), dto.getEmployRegularPaid()))

                // 4. 산재보험
                .accidentDailyMgmtNum(dto.getAccidentDailyMgmtNum())
                .accidentDailyJoinDate(dto.getAccidentDailyJoinDate())
                .accidentDailyFee(zero(dto.getAccidentDailyFee()))
                .accidentDailyPaid(zero(dto.getAccidentDailyPaid()))
                .accidentDailyRate(calcRate(dto.getAccidentDailyFee(), dto.getAccidentDailyPaid()))

                .accidentRegularMgmtNum(dto.getAccidentRegularMgmtNum())
                .accidentRegularJoinDate(dto.getAccidentRegularJoinDate())
                .accidentRegularFee(zero(dto.getAccidentRegularFee()))
                .accidentRegularPaid(zero(dto.getAccidentRegularPaid()))
                .accidentRegularRate(calcRate(dto.getAccidentRegularFee(), dto.getAccidentRegularPaid()))

                // 5. 퇴직공제
                .isSeveranceTarget(dto.getSeveranceTarget() != null ? dto.getSeveranceTarget() : false)
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

        if (dto.getPensionDailyFee() != null) {
            ins.setPensionDailyFee(zero(dto.getPensionDailyFee()));
            pensionDailyChanged = true;
        }

        if (dto.getPensionDailyPaid() != null) {
            ins.setPensionDailyPaid(zero(dto.getPensionDailyPaid()));
            pensionDailyChanged = true;
        }

        if (pensionDailyChanged) {
            ins.setPensionDailyRate(
                    calcRate(ins.getPensionDailyFee(), ins.getPensionDailyPaid())
            );
        }


        // -------------------------------
        // 국민연금 (Regular)
        // -------------------------------
        boolean pensionRegularChanged = false;

        if (dto.getPensionRegularBizSymbol() != null)
            ins.setPensionRegularBizSymbol(dto.getPensionRegularBizSymbol());

        if (dto.getPensionRegularJoinDate() != null)
            ins.setPensionRegularJoinDate(dto.getPensionRegularJoinDate());

        if (dto.getPensionRegularFee() != null) {
            ins.setPensionRegularFee(zero(dto.getPensionRegularFee()));
            pensionRegularChanged = true;
        }

        if (dto.getPensionRegularPaid() != null) {
            ins.setPensionRegularPaid(zero(dto.getPensionRegularPaid()));
            pensionRegularChanged = true;
        }

        if (pensionRegularChanged) {
            ins.setPensionRegularRate(
                    calcRate(ins.getPensionRegularFee(), ins.getPensionRegularPaid())
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

        if (dto.getHealthDailyFee() != null) {
            ins.setHealthDailyFee(zero(dto.getHealthDailyFee()));
            healthDailyChanged = true;
        }

        if (dto.getHealthDailyPaid() != null) {
            ins.setHealthDailyPaid(zero(dto.getHealthDailyPaid()));
            healthDailyChanged = true;
        }

        if (healthDailyChanged) {
            ins.setHealthDailyRate(
                    calcRate(ins.getHealthDailyFee(), ins.getHealthDailyPaid())
            );
        }


        // -------------------------------
        // 건강보험 Regular
        // -------------------------------
        boolean healthRegularChanged = false;

        if (dto.getHealthRegularBizSymbol() != null)
            ins.setHealthRegularBizSymbol(dto.getHealthRegularBizSymbol());

        if (dto.getHealthRegularJoinDate() != null)
            ins.setHealthRegularJoinDate(dto.getHealthRegularJoinDate());

        if (dto.getHealthRegularFee() != null) {
            ins.setHealthRegularFee(zero(dto.getHealthRegularFee()));
            healthRegularChanged = true;
        }

        if (dto.getHealthRegularPaid() != null) {
            ins.setHealthRegularPaid(zero(dto.getHealthRegularPaid()));
            healthRegularChanged = true;
        }

        if (healthRegularChanged) {
            ins.setHealthRegularRate(
                    calcRate(ins.getHealthRegularFee(), ins.getHealthRegularPaid())
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

        if (dto.getEmployDailyFee() != null) {
            ins.setEmployDailyFee(zero(dto.getEmployDailyFee()));
            employDailyChanged = true;
        }

        if (dto.getEmployDailyPaid() != null) {
            ins.setEmployDailyPaid(zero(dto.getEmployDailyPaid()));
            employDailyChanged = true;
        }

        if (employDailyChanged) {
            ins.setEmployDailyRate(
                    calcRate(ins.getEmployDailyFee(), ins.getEmployDailyPaid())
            );
        }


        // -------------------------------
        // 고용보험 Regular
        // -------------------------------
        boolean employRegularChanged = false;

        if (dto.getEmployRegularMgmtNum() != null)
            ins.setEmployRegularMgmtNum(dto.getEmployRegularMgmtNum());

        if (dto.getEmployRegularJoinDate() != null)
            ins.setEmployRegularJoinDate(dto.getEmployRegularJoinDate());

        if (dto.getEmployRegularFee() != null) {
            ins.setEmployRegularFee(zero(dto.getEmployRegularFee()));
            employRegularChanged = true;
        }

        if (dto.getEmployRegularPaid() != null) {
            ins.setEmployRegularPaid(zero(dto.getEmployRegularPaid()));
            employRegularChanged = true;
        }

        if (employRegularChanged) {
            ins.setEmployRegularRate(
                    calcRate(ins.getEmployRegularFee(), ins.getEmployRegularPaid())
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

        if (dto.getAccidentDailyFee() != null) {
            ins.setAccidentDailyFee(zero(dto.getAccidentDailyFee()));
            accidentDailyChanged = true;
        }

        if (dto.getAccidentDailyPaid() != null) {
            ins.setAccidentDailyPaid(zero(dto.getAccidentDailyPaid()));
            accidentDailyChanged = true;
        }

        if (accidentDailyChanged) {
            ins.setAccidentDailyRate(
                    calcRate(ins.getAccidentDailyFee(), ins.getAccidentDailyPaid())
            );
        }


        // -------------------------------
        // 산재보험 Regular
        // -------------------------------
        boolean accidentRegularChanged = false;

        if (dto.getAccidentRegularMgmtNum() != null)
            ins.setAccidentRegularMgmtNum(dto.getAccidentRegularMgmtNum());

        if (dto.getAccidentRegularJoinDate() != null)
            ins.setAccidentRegularJoinDate(dto.getAccidentRegularJoinDate());

        if (dto.getAccidentRegularFee() != null) {
            ins.setAccidentRegularFee(zero(dto.getAccidentRegularFee()));
            accidentRegularChanged = true;
        }

        if (dto.getAccidentRegularPaid() != null) {
            ins.setAccidentRegularPaid(zero(dto.getAccidentRegularPaid()));
            accidentRegularChanged = true;
        }

        if (accidentRegularChanged) {
            ins.setAccidentRegularRate(
                    calcRate(ins.getAccidentRegularFee(), ins.getAccidentRegularPaid())
            );
        }


        // -------------------------------
        // 퇴직공제
        // -------------------------------
        if (dto.getSeveranceTarget() != null)
            ins.setSeveranceTarget(dto.getSeveranceTarget());

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
