package com.example.the_labot_backend.worker;


import com.example.the_labot_backend.enums.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findAllByStatus(WorkerStatus status);
}
