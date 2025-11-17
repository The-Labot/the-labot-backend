package com.example.the_labot_backend.educations;

import com.example.the_labot_backend.educations.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {

    // 해당 현장의 교육일지 목록 조회
    List<Education> findAllBySite_IdOrderByEducationDateDesc(Long siteId);
}

