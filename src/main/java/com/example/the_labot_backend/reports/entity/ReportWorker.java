package com.example.the_labot_backend.reports.entity;

import com.example.the_labot_backend.workers.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

/**
 * 작업일보에 포함된 근로자 정보를 저장하는 엔티티
 *
 * Report : ReportWorker = 1 : N
 * 특정 작업일보가 어떤 근로자들을 포함했는지 관리
 */
@Entity
@Table(name = "report_workers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportWorker {

    // 작업일보-근로자 레코드 ID
    @Id
    @GeneratedValue
    private Long id;

    // 어떤 작업일보인지
    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    // 어떤 근로자인지
    @ManyToOne
    @JoinColumn(name = "worker_id")
    private Worker worker;
}

