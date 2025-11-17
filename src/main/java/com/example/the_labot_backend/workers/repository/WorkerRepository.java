package com.example.the_labot_backend.workers.repository;

import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findAllByStatus(WorkerStatus status);
}
