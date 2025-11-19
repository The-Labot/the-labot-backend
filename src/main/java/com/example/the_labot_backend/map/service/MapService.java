package com.example.the_labot_backend.map.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final UserRepository userRepository;
    private final FileService fileService;

    // 본인 현장 지도 조회
    public List<FileResponse> getMapByUser(String targetType, Long userId){

        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getNoticesByUser) userId:" + userId));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        // 파일 추출 (지도)
        return fileService.getFilesResponseByTarget(targetType, siteId);
    }
    
    // 특정 현장 지도 조회
    public List<FileResponse> getMapBySite(String targetType, Long siteId){
        // 파일 추출 (지도)
        return fileService.getFilesResponseByTarget(targetType, siteId);
    }
}
