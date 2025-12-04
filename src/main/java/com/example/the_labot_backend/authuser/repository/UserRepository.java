package com.example.the_labot_backend.authuser.repository;

import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import com.example.the_labot_backend.sites.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // 전화번호를 통해 user 조회
    Optional<User> findByPhoneNumber(String phoneNumber);

    List<User> findBySiteIdAndRole(Long siteId, Role role);

    // siteID를 통해 해당 역할의 인원 수 조회
    int countBySite_IdAndRole(Long siteId, Role role);

    // headOfficeId를 통해 해당 역할의 인원 수 조회
    int countByHeadOffice_IdAndRole(Long headOfficeId, Role role);

    List<User> findByHeadOfficeAndSiteAndRole(HeadOffice headOffice, Site site, Role role);

}
