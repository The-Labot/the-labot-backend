package com.example.the_labot_backend.hazards;

import com.example.the_labot_backend.hazards.entity.Hazard;
import com.example.the_labot_backend.notices.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HazardRepository extends JpaRepository<Hazard,Long> {
    List<Hazard> findAllBySite_Id(Long siteId);
}
