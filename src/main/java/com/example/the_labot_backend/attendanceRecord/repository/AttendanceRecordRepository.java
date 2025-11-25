package com.example.the_labot_backend.attendanceRecord.repository;

import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {


    List<AttendanceRecord> findByWorkerIdAndWorkDateBetween(Long workerId, LocalDate start, LocalDate end);

    Optional<AttendanceRecord> findByWorkerIdAndWorkDate(Long workerId, LocalDate workDate);
}
