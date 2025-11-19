package com.example.the_labot_backend.headoffice.repository;

import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeadOfficeRepository extends JpaRepository<HeadOffice, Long> {
    // 본사코드로 본사 찾기
    Optional<HeadOffice> findBySecretCode(String privateCond);
}
