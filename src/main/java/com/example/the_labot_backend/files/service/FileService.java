package com.example.the_labot_backend.files.service;

import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.repository.FileRepository;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    //스프링이 제공하는 aws s3 만능 리모컨
    private final S3Template s3Template;

    //application.yml에 설정한 버킷 이름 가져오기 <-- 파일을 담을 최상위 폴더
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    //파일저장
    @Transactional
    public void saveFiles(List<MultipartFile> multipartFiles, String targetType, Long targetId) {
        //파일 비어있으면 안해
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return;
        }

        //하나씩 꺼내서 순차적으로 처리할거임
        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile.isEmpty()) continue;

            //원본 파일명 가져와서
            String originalName = multipartFile.getOriginalFilename();
            //uuid 붙일거임
            String uuid = UUID.randomUUID().toString();

            //파일종류별로 폴더 나눌거임
            //targetType이 "SITE"이면 site/ 라는 문자열 만듦
            //s3는 폴더명/ 붙이면 자동으로 폴더 안에 저장해줌
            //s3는 사실 폴더라는 개념이 없음 우리한테는 그렇게 보여질뿐 걍 경로임
            String folderPath = targetType.toLowerCase() + "/";

            //s3에 저장될 최종 이름
            String storedName = folderPath + uuid + "_" + originalName;

            try {
                //파일 내용물 준비. 파일을 메모리에 다 로딩안하고 빨대 꽂아서 S3로 흘려보냄 <-- 메모리 절약됨
                //자바 표준 라이브러리. java.io
                InputStream inputStream = multipartFile.getInputStream();

                //파일 메타데이터 설정
                //jpg인지 pdf인지, 크기등 알려주는 명찰
                //이거안하면 다운은 되지만 브라우저에서 안열릴수있음
                ObjectMetadata metadata = ObjectMetadata.builder()
                        .contentType(multipartFile.getContentType())
                        .contentLength(multipartFile.getSize())
                        .build();

                //s3업로드 실행함
                //s3Template이 알아서 aws랑 통신해서 파일 업로드함
                S3Resource resource = s3Template.upload(bucket, storedName, inputStream, metadata);

                //업로드 된 파일의 주소 가져옴
                String fileUrl = resource.getURL().toString();

                //db에 저장할 엔티티 만들기
                File fileEntity = File.builder()
                        .originalFileName(originalName)
                        .storedFileName(storedName)
                        .fileUrl(fileUrl) // 로컬 경로(/uploads/...) 대신 S3 URL(https://...) 저장
                        .contentType(multipartFile.getContentType())
                        .size(multipartFile.getSize())
                        .targetType(targetType)
                        .targetId(targetId)
                        .build();

                //db에 주소 저장
                fileRepository.save(fileEntity);

            } catch (IOException e) {
                throw new RuntimeException("S3 파일 업로드 실패: " + originalName, e);
            }
        }
    }

    //파일삭제
    @Transactional
    public void deleteFilesByTarget(String targetType, Long targetId) {
        //db에서 파일 찾기
        List<File> files = fileRepository.findByTargetTypeAndTargetId(targetType, targetId);

        for (File file : files) {
            //s3에서 실제 파일 삭제
            s3Template.deleteObject(bucket, file.getStoredFileName());

            //db에서 url 삭제
            fileRepository.delete(file);
        }
    }

    //조회하는건 수정없음. url 반환만 해주면 됨
    @Transactional(readOnly = true)
    public List<File> getFilesByTarget(String targetType, Long targetId) {
        return fileRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }

    //얘는 위에 조회랑 다른게 뭐지?
    @Transactional(readOnly = true)
    public List<FileResponse> getFilesResponseByTarget(String targetType, Long targetId) {
        List<File> files = fileRepository.findByTargetTypeAndTargetId(targetType, targetId);
        return FileResponse.fromList(files);
    }
}
