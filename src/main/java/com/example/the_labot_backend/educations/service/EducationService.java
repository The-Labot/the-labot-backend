package com.example.the_labot_backend.educations.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.educations.dto.EducationCreateRequest;
import com.example.the_labot_backend.educations.dto.EducationDetailResponse;
import com.example.the_labot_backend.educations.dto.EducationListResponse;
import com.example.the_labot_backend.educations.entity.Education;
import com.example.the_labot_backend.educations.entity.EducationParticipant;
import com.example.the_labot_backend.educations.repository.EducationParticipantRepository;
import com.example.the_labot_backend.educations.repository.EducationRepository;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.global.exception.BadRequestException;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final WorkerRepository workerRepository;
    private final EducationParticipantRepository participantRepository;

    // 안전교육일지 등록
    @Transactional
    public void createEducation(
            Long userId,
            EducationCreateRequest request,
            List<MultipartFile> materials,
            List<MultipartFile> photos,
            List<MultipartFile> signatures
    ) {
        // --- [1] 필수값 검증 ---
        validateRequiredFields(request);

        // --- 1. 사용자/현장 검증 ---
        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Site site = writer.getSite();

        // --- 2. 교육일지 생성 ---
        Education education = Education.builder()
                .site(site)
                .writer(writer)
                .createdDate(LocalDate.now())
                .educationTitle(request.getEducationTitle())
                .educationDate(request.getEducationDate())
                .educationTime(request.getEducationTime())
                .educationPlace(request.getEducationPlace())
                .educationType(request.getEducationType())
                .instructor(request.getInstructor())
                .content(request.getContent())
                .status(request.getStatus())
                .result(request.getResult())
                .specialNote(request.getSpecialNote())
                .build();

        educationRepository.save(education);

        // --- 3. 교육 대상자 저장 ---
        if (request.getParticipantIds() != null) {
            for (Long workerId : request.getParticipantIds()) {

                Worker worker = workerRepository.findById(workerId)
                        .orElseThrow(() -> new NotFoundException("근로자를 찾을 수 없습니다: " + workerId));

                EducationParticipant participant =
                        EducationParticipant.builder()
                                .education(education)
                                .worker(worker)
                                .build();

                participantRepository.save(participant);
            }
        }

        // --- 4. 파일 업로드 (교육자료 / 사진 / 서명PDF) ---
        fileService.saveFiles(materials, "EDUCATION-MATERIAL", education.getId());
        fileService.saveFiles(photos, "EDUCATION-PHOTO", education.getId());
        fileService.saveFiles(signatures, "EDUCATION-SIGNATURE", education.getId());

    }

    // userId를 통해 현장별 안전교육일지 목록 조회
    @Transactional(readOnly = true)
    public List<EducationListResponse> getEducationByUser(Long userId) {

        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다.(getReportsByUser) userId:" + userId));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        return educationRepository.findAllBySite_IdOrderByEducationDateDesc(siteId)
                .stream()
                .map(EducationListResponse::from)
                .collect(Collectors.toList());
    }

    // 안전교육일지 상세 조회
    @Transactional(readOnly = true)
    public EducationDetailResponse getEducationDetail(Long userId, Long educationId) {
        Education edu = educationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("안전교육일지를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if(!edu.getSite().getId().equals(user.getSite().getId())){
            throw new ForbiddenException("해당 안전교육일지에 접근할 권한이 없습니다.");
        }

        List<EducationParticipant> participants =
                participantRepository.findAllByEducationId(edu.getId());

        // 파일 조회
        List<File> materialFiles =
                fileService.getFilesByTarget("EDUCATION-MATERIAL", edu.getId());

        List<File> photoFiles =
                fileService.getFilesByTarget("EDUCATION-PHOTO", edu.getId());

        List<File> signatureFiles =
                fileService.getFilesByTarget("EDUCATION-SIGNATURE", edu.getId());

        return EducationDetailResponse.from(edu,participants,materialFiles,photoFiles,signatureFiles);
    }

    // 안전교육일지 수정
    @Transactional
    public void updateEducation(
            Long userId,
            Long educationId,
            EducationCreateRequest req,
            List<MultipartFile> materials,
            List<MultipartFile> photos,
            List<MultipartFile> signatures
    ) {

        validateRequiredFields(req);

        // --- [2] 사용자 조회 ---
        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("교육일지를 찾을 수 없습니다."));

        if(!education.getSite().getId().equals(writer.getSite().getId())){
            throw new ForbiddenException("해당 안전교육일지에 접근할 권한이 없습니다.");
        }

        education.setEducationTitle(req.getEducationTitle());
        education.setEducationDate(req.getEducationDate());
        education.setEducationTime(req.getEducationTime());
        education.setEducationPlace(req.getEducationPlace());
        education.setEducationType(req.getEducationType());
        education.setInstructor(req.getInstructor());
        education.setContent(req.getContent());
        education.setStatus(req.getStatus());
        education.setResult(req.getResult());
        education.setSpecialNote(req.getSpecialNote());

        // --- [5] 참여 근로자 업데이트 ---
        // 기존 참가자 전체 삭제
        participantRepository.deleteAllByEducation(education);

        // 새 참가자 추가
        if (req.getParticipantIds() != null) {
            for (Long workerId : req.getParticipantIds()) {

                Worker worker = workerRepository.findById(workerId)
                        .orElseThrow(() -> new NotFoundException("근로자를 찾을 수 없습니다. ID=" + workerId));

                participantRepository.save(
                        EducationParticipant.builder()
                                .education(education)
                                .worker(worker)
                                .build()
                );
            }
        }

        // 기존 파일 삭제
        fileService.deleteFilesByTarget("EDUCATION-MATERIAL", education.getId());
        fileService.deleteFilesByTarget("EDUCATION-PHOTO", education.getId());
        fileService.deleteFilesByTarget("EDUCATION-SIGNATURE", education.getId());

        // 새 파일 저장
        fileService.saveFiles(materials, "EDUCATION-MATERIAL", education.getId());
        fileService.saveFiles(photos, "EDUCATION-PHOTO", education.getId());
        fileService.saveFiles(signatures, "EDUCATION-SIGNATURE", education.getId());
    }

    @Transactional
    public void deleteEducation(Long userId, Long educationId) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 교육일지 조회
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("교육일지를 찾을 수 없습니다."));

        if(!education.getSite().getId().equals(user.getSite().getId())){
            throw new ForbiddenException("해당 안전교육일지에 접근할 권한이 없습니다.");
        }

        // 3. 교육 참여자 전부 삭제
        participantRepository.deleteAllByEducation(education);

        // 4. 관련 파일 삭제
        fileService.deleteFilesByTarget("EDUCATION-MATERIAL", education.getId());
        fileService.deleteFilesByTarget("EDUCATION-PHOTO", education.getId());
        fileService.deleteFilesByTarget("EDUCATION-SIGNATURE", education.getId());

        // 5. 메인 교육일지 삭제
        educationRepository.delete(education);
    }

    // ============================================================
    // 필수값 검증 메서드
    // ============================================================
    private void validateRequiredFields(EducationCreateRequest req) {

        if (req.getEducationTitle() == null || req.getEducationTitle().trim().isEmpty()) {
            throw new BadRequestException("교육 제목은 필수 입력 항목입니다.");
        }

        if (req.getEducationDate() == null) {
            throw new BadRequestException("교육 날짜는 필수 입력 항목입니다.");
        }

        if (req.getEducationTime() == null || req.getEducationTime().trim().isEmpty()) {
            throw new BadRequestException("교육 시간은 필수 입력 항목입니다.");
        }

        if (req.getEducationPlace() == null || req.getEducationPlace().trim().isEmpty()) {
            throw new BadRequestException("교육 장소는 필수 입력 항목입니다.");
        }

        if (req.getEducationType() == null) {
            throw new BadRequestException("교육 구분은 필수 입력 항목입니다.");
        }

        if (req.getInstructor() == null || req.getInstructor().trim().isEmpty()) {
            throw new BadRequestException("강사명은 필수 입력 항목입니다.");
        }

        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new BadRequestException("교육 내용은 필수 입력 항목입니다.");
        }
    }
}
