package com.example.the_labot_backend.hazards.repository;

import com.example.the_labot_backend.hazards.entity.Hazard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HazardRepository extends JpaRepository<Hazard,Long> {
    List<Hazard> findAllBySite_Id(Long siteId);
}
