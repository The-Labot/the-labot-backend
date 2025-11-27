package com.example.the_labot_backend.reports.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 작업일보에 포함된 “장비 투입 내역”을 저장하는 엔티티
 *
 * Report : ReportEquipment = 1 : N
 * 특정 작업일보에 어떤 장비가 투입되었는지 관리
 */
@Entity
@Table(name = "report_equipment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 장비 투입 기록 고유 ID

    // 어떤 작업일보인지
    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    private String equipmentName;
    // 장비명 (예: 굴삭기, 크레인, 덤프트럭 등)

    private String spec;
    // 장비 규격 (예: 05톤, 25톤, 3톤, 12M 고소작업대 등)

    private String usingTime;
    // 투입 시간 (예: "08:00~17:00" 또는 단순 문자열)

    private Integer count;
    // 장비 대수 (예: 1대, 2대)

    private String vendorName;
    // 장비 대여 업체명 (예: ABC중기, XX크레인 등)
}
