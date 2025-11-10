package com.example.the_labot_backend.files;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByTargetTypeAndTargetId(String targetType, Long targetId);
}
