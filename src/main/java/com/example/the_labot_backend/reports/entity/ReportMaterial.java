package com.example.the_labot_backend.reports.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 작업일보에 포함된 “자재 반입/반출 내역”을 저장하는 엔티티
 *
 * Report : ReportMaterial = 1 : N
 * 특정 작업일보에서 어떤 자재가 들어오고 나갔는지 관리
 */
@Entity
@Table(name = "report_material")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 자재 기록 고유 ID

    // 어떤 작업일보인지
    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    private String materialName;
    // 자재명 (예: 철근, 레미콘, 합판, 몰탈 등)

    private String specAndQuantity;
    // 자재 규격 및 수량 (예: D13 2톤, 25-210 레미콘 5대)

    private String importTime;
    // 반입 시간 (예: "13:20", "오전", "오후" 등)

    private String exportDetail;
    // 반출 내용 (예: "잔재 철거물 반출", "폐기물 3톤 반출", "철근 여분 회수" 등)
}

