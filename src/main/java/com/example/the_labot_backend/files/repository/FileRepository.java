package com.example.the_labot_backend.files.repository; //패키지 이름 잘못되어있어서 .repository추가함. 11/19 6:39박찬홍

import com.example.the_labot_backend.files.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByTargetTypeAndTargetId(String targetType, Long targetId);
}
