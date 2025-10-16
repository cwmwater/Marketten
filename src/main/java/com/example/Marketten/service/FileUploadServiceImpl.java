package com.example.Marketten.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor // ✨ 생성자 주입을 위해 추가
public class FileUploadServiceImpl implements FileUploadService {

    // ✨ application.yml에서 정의한 절대 경로를 주입받습니다.
    @Value("${file.upload.path}")
    private String uploadPath;

    // ✨ application.yml에서 정의한 URL 접두사를 주입받습니다.
    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public String uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null; // 파일이 없으면 null 반환
        }

        // 1. 고유한 파일 이름 생성 (중복 방지)
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf("."));
        String savedName = UUID.randomUUID().toString() + extension;

        // 2. 디렉토리가 없으면 생성
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            log.error("Could not create upload directory!", e);
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다.");
        }

        // 3. 최종 저장 경로 설정 (절대 경로 사용)
        // 예: C:/marketten/uploads/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.jpg
        Path targetPath = Paths.get(uploadPath).resolve(savedName);

        // 4. 파일 저장
        try {
            file.transferTo(targetPath);
        } catch (IOException e) {
            log.error("Failed to save file", e);
            throw new RuntimeException("파일 저장에 실패했습니다.");
        }

        log.info("File uploaded successfully to: {}", targetPath);

        // ✨ 5. 파일 이름이나 전체 URL이 아닌, WebConfig와 약속된 URL 경로를 반환
        // 예: /images/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx.jpg
        return urlPrefix + savedName;
    }
}