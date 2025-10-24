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

    private final RestTemplate restTemplate;
    private final String BASE_URL = "http://marketten-module:8000/gpt";

    public Map<String, Object> postToFastApi(String path, Object request) {
        try {
            String fullUrl = BASE_URL + path;
            Map<String, Object> response = restTemplate.postForObject(fullUrl, request, Map.class);
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return (Map<String, Object>) response.get("data");
            } else {
                throw new RuntimeException("FastAPI error: " + response.get("error"));
            }
        } catch (Exception e) {
            throw new RuntimeException("FastAPI 호출 실패: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> analyzeContentKeywords(ContentKeywordRequest request) {
        return postToFastApi("/content/keywords", request);
    }

    public String generateContent(ContentGenerateRequest request) {
        Map<String, Object> result = postToFastApi("/content", request);
        return (String) result.get("content");
    }

    public Map<String, Object> analyzeTitleKeywords(TitleKeywordRequest request) {
        return postToFastApi("/titles/keywords", request);
    }

    public Set<String> generateTitles(TitleGenerateRequest request) {
        Map<String, Object> result = postToFastApi("/titles", request);
        return new HashSet<>((List<String>) result.get("titles"));
    }
}

