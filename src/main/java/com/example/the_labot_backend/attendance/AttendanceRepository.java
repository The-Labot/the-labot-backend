package com.example.the_labot_backend.attendance;

import com.example.the_labot_backend.workers.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByWorkerAndDate(Worker worker, LocalDate date);
}
