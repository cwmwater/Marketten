package com.example.Marketten.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileUploadService {

    /**
     * 프로필 이미지 파일을 서버에 저장하고, 저장된 파일의 URL 경로를 반환합니다.
     *
     * @param file 업로드할 파일 객체
     * @return 서버에 저장된 파일의 접근 가능한 URL
     */
    String uploadProfileImage(MultipartFile file);
}