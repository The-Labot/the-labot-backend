package com.example.the_labot_backend.educations.repository;

import com.example.the_labot_backend.educations.entity.Education;
import com.example.the_labot_backend.educations.entity.EducationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationParticipantRepository extends JpaRepository<EducationParticipant,Long> {
    List<EducationParticipant> findAllByEducationId(Long educationId);
    void deleteAllByEducation(Education education);
}
