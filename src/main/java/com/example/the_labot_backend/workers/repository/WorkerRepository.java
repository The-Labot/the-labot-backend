package com.example.the_labot_backend.workers.repository;

import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findAllByStatus(WorkerStatus status);

    @Query("SELECT w FROM Worker w " +
            "JOIN FETCH w.user u " +
            "JOIN FETCH u.site s " +
            "WHERE u.headOffice.id = :headOfficeId")
    List<Worker> findAllByHeadOfficeId(@Param("headOfficeId") Long headOfficeId);
    List<Worker> findByUser_Site_IdAndStatusNot(Long siteId, WorkerStatus status);


    @Query("""
    SELECT w 
    FROM Worker w
    WHERE w.user.site.id = :siteId
    """)
    List<Worker> findWorkersBySiteId(@Param("siteId") Long siteId);

    List<Worker> findByIdIn(List<Long> userIds);
    // [★ 신규 추가] 내 현장의 '퇴직자가 아닌' 근로자 총원 카운트
    long countByUser_Site_IdAndStatusNot(Long siteId, WorkerStatus status);
    // [★ 핵심] 특정 현장의, 특정 상태(ACTIVE)인 근로자 수 카운트
    long countByUser_Site_IdAndStatus(Long siteId, WorkerStatus status);
}
