package com.example.Marketten.service;

import com.example.Marketten.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    // application.yml에서 설정한 파일 저장 경로 주입 (예: upload)
    @Value("${marketten.upload.path}")
    private String uploadPath;

    @Value("${server.port}")
    private String serverPort; // 서버 포트

    @Value("${server.host:localhost}")
    private String serverHost; // 서버 호스트 (기본값: localhost)

    /**
     * 프로필 이미지 파일을 서버에 저장하고, 저장된 파일의 URL 경로를 반환합니다.
     */
    @Override
    public String uploadProfileImage(MultipartFile file) {
        if (file.isEmpty()) {
            // 파일이 비어있다면, 파일을 저장하지 않고 null 또는 기본 URL 반환 가능
            // 여기서는 RuntimeException을 던져 파일 저장이 필수임을 가정합니다.
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        // 1. 파일 이름 생성 (UUID를 사용하여 중복 방지)
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uuid = UUID.randomUUID().toString();
        String savedFilename = uuid + extension;

        // 2. 저장 경로 설정 및 디렉토리 생성
        // targetPath = /프로젝트루트/upload/profile/
        Path directoryPath = Paths.get(uploadPath, "profile");

        try {
            // 디렉토리가 없으면 생성 (재시도 필요 없음)
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", directoryPath, e);
            throw new RuntimeException("업로드 디렉토리 생성에 실패했습니다.", e);
        }

        // 3. 최종 파일 경로 설정
        Path targetPath = directoryPath.resolve(savedFilename);

        // 4. 파일 저장 실행 (java.nio.file.Path 사용으로 권한 문제 회피)
        try {
            file.transferTo(targetPath); // 파일을 디스크에 저장
        } catch (IOException e) {
            log.error("Failed to save file: {}", savedFilename, e);
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }

        log.info("File uploaded successfully. Path: {}", targetPath.toAbsolutePath());

        // 5. 접근 가능한 URL 반환
        // 예: http://localhost:8080/upload/profile/uuid.jpg
        // Note: '/upload/' 경로는 정적 리소스 설정 (WebMvcConfigurer)이 필요합니다.
        return String.format("http://%s:%s/%s/%s/%s", serverHost, serverPort, uploadPath, "profile", savedFilename);
    }
}