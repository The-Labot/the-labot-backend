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
}
