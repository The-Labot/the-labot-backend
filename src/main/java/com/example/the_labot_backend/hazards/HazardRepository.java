package com.example.the_labot_backend.hazards;

import com.example.the_labot_backend.hazards.entity.Hazard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HazardRepository extends JpaRepository<Hazard,Long> {
}
