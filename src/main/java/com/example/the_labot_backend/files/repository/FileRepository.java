package com.example.the_labot_backend.files.repository;

import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.entity.FileTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByTargetTypeAndTargetId(FileTargetType targetType, Long targetId);
}
