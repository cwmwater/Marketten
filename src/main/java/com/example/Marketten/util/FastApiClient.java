package com.example.Marketten.util;

import com.example.Marketten.dto.gpt.ContentGenerateRequest;
import com.example.Marketten.dto.gpt.ContentKeywordRequest;
import com.example.Marketten.dto.gpt.TitleGenerateRequest;
import com.example.Marketten.dto.gpt.TitleKeywordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FastApiClient {

    private final RestTemplate restTemplate; // HTTP 요청을 보내기 위한 RestTemplate
    private final String BASE_URL = "http://localhost:8000/gpt"; // FastAPI 서버 기본 URL

    // FastAPI 서버에 POST 요청을 보내고 결과를 반환
    public Map<String, Object> postToFastApi(String path, Object request) {
        try {
            String fullUrl = BASE_URL + path; // 전체 요청 URL 구성
            Map<String, Object> response = restTemplate.postForObject(fullUrl, request, Map.class); // 요청 전송

            // 응답이 성공이면 데이터 반환
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return (Map<String, Object>) response.get("data");
            } else {
                // 실패 시 예외 발생
                throw new RuntimeException("FastAPI error: " + response.get("error"));
            }
        } catch (Exception e) {
            // 호출 자체 실패 시 예외 처리
            throw new RuntimeException("FastAPI 호출 실패: " + e.getMessage(), e);
        }
    }

    // 콘텐츠 키워드 분석 요청
    public Map<String, Object> analyzeContentKeywords(ContentKeywordRequest request) {
        return postToFastApi("/content/keywords", request);
    }

    // 콘텐츠 생성 요청
    public String generateContent(ContentGenerateRequest request) {
        Map<String, Object> result = postToFastApi("/content", request);
        return (String) result.get("content");
    }

    // 제목 키워드 분석 요청
    public Map<String, Object> analyzeTitleKeywords(TitleKeywordRequest request) {
        return postToFastApi("/titles/keywords", request);
    }

    // 제목 생성 요청
    public Set<String> generateTitles(TitleGenerateRequest request) {
        Map<String, Object> result = postToFastApi("/titles", request);
        return new HashSet<>((List<String>) result.get("titles"));
    }
}
