package com.example.the_labot_backend.educations.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.educations.dto.EducationListResponse;
import com.example.the_labot_backend.educations.dto.EducationRequest;
import com.example.the_labot_backend.educations.dto.EducationResponse;
import com.example.the_labot_backend.educations.entity.Education;
import com.example.the_labot_backend.educations.repository.EducationRepository;
import com.example.the_labot_backend.sites.entity.Site;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final UserRepository userRepository;

    // 안전교육일지 등록
    @Transactional
    public EducationResponse createEducation(Long userId, EducationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getReportsByUser) userId:" + userId));

        Site site = user.getSite();

        Education education = Education.builder()
                .writer(user)
                .site(site)
                .educationDate(request.getEducationDate())
                .course(request.getCourse())
                .participants(request.getParticipants())
                .subject(request.getSubject())
                .content(request.getContent())
                .instructor(request.getInstructor())
                .location(request.getLocation())
                .note(request.getNote())
                .build();

        return EducationResponse.from(educationRepository.save(education));
    }

    // userId를 통해 현장별 안전교육일지 목록 조회
    @Transactional(readOnly = true)
    public List<EducationListResponse> getEducationByUser(Long userId) {

        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getReportsByUser) userId:" + userId));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        return educationRepository.findAllBySite_IdOrderByEducationDateDesc(siteId)
                .stream()
                .map(EducationListResponse::from)
                .collect(Collectors.toList());
    }

    // 안전교육일지 상세 조회
    @Transactional(readOnly = true)
    public EducationResponse getEducationDetail(Long eduId) {
        Education edu = educationRepository.findById(eduId)
                .orElseThrow(() -> new RuntimeException("교육일지를 찾을 수 없습니다."));

        return EducationResponse.from(edu);
    }

    // 안전교육일지 수정
    @Transactional
    public EducationResponse updateEducation(Long eduId, EducationRequest request) {

        Education edu = educationRepository.findById(eduId)
                .orElseThrow(() -> new RuntimeException("교육일지를 찾을 수 없습니다."));

        edu.setEducationDate(request.getEducationDate());
        edu.setCourse(request.getCourse());
        edu.setParticipants(request.getParticipants());
        edu.setSubject(request.getSubject());
        edu.setContent(request.getContent());
        edu.setInstructor(request.getInstructor());
        edu.setLocation(request.getLocation());
        edu.setNote(request.getNote());

        return EducationResponse.from(edu);
    }

    // 안전교육일지 삭제
    @Transactional
    public void delete(Long eduId) {
        educationRepository.deleteById(eduId);
    }
}
