package com.example.the_labot_backend.headoffice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeadOfficeRepository extends JpaRepository<HeadOffice, Long> {
    Optional<HeadOffice> findBySecretCode(String privateCond);
}
