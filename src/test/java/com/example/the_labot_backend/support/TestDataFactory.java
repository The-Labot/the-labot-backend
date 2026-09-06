package com.example.the_labot_backend.support;

import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.educations.entity.Education;
import com.example.the_labot_backend.educations.entity.EducationStatus;
import com.example.the_labot_backend.educations.entity.EducationType;
import com.example.the_labot_backend.educations.repository.EducationRepository;
import com.example.the_labot_backend.hazards.entity.Hazard;
import com.example.the_labot_backend.hazards.entity.HazardStatus;
import com.example.the_labot_backend.hazards.repository.HazardRepository;
import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import com.example.the_labot_backend.headoffice.repository.HeadOfficeRepository;
import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import com.example.the_labot_backend.notices.repository.NoticeRepository;
import com.example.the_labot_backend.reports.entity.Report;
import com.example.the_labot_backend.reports.repository.ReportRepository;
import com.example.the_labot_backend.sites.entity.ContractType;
import com.example.the_labot_backend.sites.entity.InsuranceResponsibility;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import com.example.the_labot_backend.workers.entity.embeddable.WorkerBankAccount;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 스모크 테스트용 최소 데이터.
 *
 * 본사 1 → 현장 1 → 사용자 3(관리자/현장관리자/근로자) → 도메인 데이터 각 1건.
 * 조회 API가 빈 목록이 아니라 실제 데이터를 직렬화하도록 만드는 것이 목적이다.
 * (빈 목록은 지연 로딩 경로를 밟지 않아 검증이 되지 않는다)
 */
@Component
@RequiredArgsConstructor
@Getter
public class TestDataFactory {

    private final HeadOfficeRepository headOfficeRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final NoticeRepository noticeRepository;
    private final HazardRepository hazardRepository;
    private final ReportRepository reportRepository;
    private final EducationRepository educationRepository;

    private Long siteId;
    private Long adminId;
    private Long managerId;
    private Long workerId;
    private Long noticeId;
    private Long hazardId;
    private Long reportId;
    private Long educationId;

    private boolean prepared = false;

    @Transactional
    public void prepareOnce() {
        if (prepared) {
            return;
        }

        HeadOffice headOffice = headOfficeRepository.save(HeadOffice.builder()
                .name("테스트본사")
                .secretCode("secret")
                .address("서울시 중구")
                .phoneNumber("021112222")
                .representative("대표자")
                .build());

        Site site = siteRepository.save(Site.builder()
                .headOffice(headOffice)
                .projectName("테스트 현장 신축공사")
                .contractType(ContractType.PRIME)
                .siteManagerName("현장소장")
                .contractAmount(1_000_000_000L)
                .clientName("발주처")
                .primeContractorName("원도급사")
                .address("서울시 강남구")
                .latitude(37.4979)
                .longitude(127.0276)
                .contractDate(LocalDate.now().minusMonths(6))
                .startDate(LocalDate.now().minusMonths(5))
                .endDate(LocalDate.now().plusMonths(6))
                .insuranceResponsibility(InsuranceResponsibility.ALL)
                .build());
        siteId = site.getId();

        User admin = userRepository.save(User.builder()
                .phoneNumber("01000000001")
                .password("{noop}test")
                .name("테스트관리자")
                .headOffice(headOffice)
                .role(Role.ROLE_ADMIN)
                .build());
        adminId = admin.getId();

        User manager = userRepository.save(User.builder()
                .phoneNumber("01000000002")
                .password("{noop}test")
                .name("테스트현장관리자")
                .site(site)
                .headOffice(headOffice)
                .role(Role.ROLE_MANAGER)
                .build());
        managerId = manager.getId();

        User workerUser = userRepository.save(User.builder()
                .phoneNumber("01000000003")
                .password("{noop}test")
                .name("테스트근로자")
                .site(site)
                .headOffice(headOffice)
                .role(Role.ROLE_WORKER)
                .build());
        workerId = workerUser.getId();

        workerRepository.save(Worker.builder()
                .user(workerUser)
                .address("서울시 송파구")
                .gender("남성")
                .birthDate(LocalDate.of(1990, 1, 1))
                .nationality("대한민국")
                .status(WorkerStatus.ACTIVE)
                .position("목공")
                .siteName("테스트 현장")
                .emergencyNumber("01099998888")
                .contractType("일용직")
                .salary("150000")
                .payReceive("20")
                .bankAccount(new WorkerBankAccount("테스트은행", "1234567890", "테스트근로자"))
                .build());

        noticeId = noticeRepository.save(Notice.builder()
                .writer(manager)
                .site(site)
                .title("테스트 공지")
                .content("테스트 공지 내용")
                .category(NoticeCategory.SAFETY)
                .urgent(false)
                .pinned(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()).getId();

        hazardId = hazardRepository.save(Hazard.builder()
                .reporter(workerUser)
                .site(site)
                .hazardType("추락")
                .location("3층 개구부")
                .description("안전난간 미설치")
                .urgent(true)
                .status(HazardStatus.WAITING)
                .reportedAt(LocalDateTime.now())
                .build()).getId();

        reportId = reportRepository.save(Report.builder()
                .writer(manager)
                .site(site)
                .createdAt(LocalDateTime.now())
                .workType("골조")
                .workDate(LocalDate.now())
                .todayWork("3층 슬래브 타설")
                .tomorrowPlan("양생")
                .workerCount(12)
                .workLocation("3층")
                .specialNote("특이사항 없음")
                .build()).getId();

        educationId = educationRepository.save(Education.builder()
                .writer(manager)
                .site(site)
                .createdDate(LocalDate.now())
                .educationTitle("정기 안전교육")
                .educationDate(LocalDate.now())
                .educationTime("09:00~10:00")
                .educationPlace("현장 회의실")
                .educationType(EducationType.REGULAR)
                .instructor("안전관리자")
                .content("추락 재해 예방")
                .status(EducationStatus.COMPLETED)
                .specialNote("없음")
                .result("이수")
                .build()).getId();

        prepared = true;
    }
}
