package com.example.the_labot_backend.sites.repository;

import com.example.the_labot_backend.sites.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long> {
}
