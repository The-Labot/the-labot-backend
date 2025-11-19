package com.example.the_labot_backend.sites.repository;

import com.example.the_labot_backend.sites.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {

    // 해당 본사의 현장 목록 조회
    List<Site> findAllByHeadOffice_Id(Long headOfficeId);

}
