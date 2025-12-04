package com.example.the_labot_backend.map.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapService {

    private final UserRepository userRepository;
    private final FileService fileService;

    // 지도 등록 / 수정
    @Transactional
    public void uploadMap(Long userId, List<MultipartFile> files){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId:" + userId));

        
        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        // 파일 삭제
        fileService.deleteFilesByTarget("SITE_MAP", siteId);
        
        // 파일 저장
        fileService.saveFiles(files, "SITE_MAP", siteId);
    }

    // 본인 현장 지도 조회
    @Transactional(readOnly = true)
    public List<FileResponse> getMapByUser(Long userId){
        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.(getNoticesByUser) userId:" + userId));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        // 파일 추출 (지도)
        return fileService.getFilesResponseByTarget("SITE_MAP", siteId);
    }

    // 특정 현장 지도 조회
    @Transactional(readOnly = true)
    public List<FileResponse> getMapBySite(String targetType, Long siteId){
        // 파일 추출 (지도)
        return fileService.getFilesResponseByTarget(targetType, siteId);
    }

    // 지도 삭제
    @Transactional
    public void deleteMap(Long userId){
        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.(getNoticesByUser) userId:" + userId));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        fileService.deleteFilesByTarget("SITE_MAP", siteId);
    }

}
