package com.example.the_labot_backend.sites.entity;

import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import com.example.the_labot_backend.sites.entity.embeddable.BankAccount;
import com.example.the_labot_backend.sites.entity.embeddable.SiteSocialIns;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Long id; // 현장 ID

    @ManyToOne
    @JoinColumn(name = "headoffice_id")
    private HeadOffice headOffice;

    // 입력 해야하는 값들

    // ==========================================
    // 1. 현장 개요
    // ==========================================

    // 현장 개요
    
    @Column(nullable = false, length = 100)
    private String projectName; // 공사명 (예: 2009년 목포대학교 학생회관 신축공사)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType contractType; // 도급종류 (PRIME: 원도급, SUB: 하도급)

    @Column(length = 50)
    private String siteManagerName; // 현장대리인 (현장 소장)

    // 계약 정보
    
    private Long contractAmount; // 도급금액 (계약 금액, 0일 수 있음)

    @Column(length = 100)
    private String clientName; // 도급처 (돈 주는 곳)

    @Column(length = 100)
    private String primeContractorName; // 원도급사 (공공/민간 구분 또는 상위 건설사)

    // ==========================================
    // 2. 위치 정보 (지도/출퇴근 체크용)
    // ==========================================
    @Column(nullable = false)
    private String address; // 공사 현장 소재지 (주소)

    // gps이용을 위해서 추가함. 11/17 박찬홍
    @Column(nullable = false) // (출퇴근 기능이 필수라면 false로 설정)
    private Double latitude; // 현장 위도 (기준점)

    @Column(nullable = false)
    private Double longitude; // 현장 경도 (기준점)

    // ==========================================
    // 3. 공사 기간
    // ==========================================
    private LocalDate contractDate; // 계약일
    private LocalDate startDate;    // 착공일
    private LocalDate endDate;      // 준공일

    // ==========================================
    // 4. 노무비 구분관리제 (전용 계좌)
    // ==========================================
    @Embedded
    private BankAccount laborCostAccount; // 자재비와 분리된 인건비 전용 통장 정보

    // ==========================================
    // 5. 4대 보험 및 행정 정보
    // ==========================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InsuranceResponsibility insuranceResponsibility;

    @Column(length = 11, unique = true)
    private String employmentInsuranceSiteNum; // 4대보험 고용번호 (사업장 관리번호 11자리, 근로자 실업급여용)

    @Column(length = 20)
    private String primeContractorMgmtNum; // 원수급번호 (하도급일 경우 원청의 관리번호 기입)

    @Embedded
    private SiteSocialIns socialIns;

    // ==========================================
    // 6. KISCON (건설공사대장 통보)
    // ==========================================

    private boolean isKisconReportTarget; // 키스콘 전자통보 대상 여부
    // 로직 Tip: 계약일로부터 30일 체크는 Service 계층에서 contractDate 기준으로 스케줄러/배치로 확인

}
