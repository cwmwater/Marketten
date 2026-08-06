package com.example.Marketten.service;

import com.example.Marketten.domain.ToneExample;
import com.example.Marketten.domain.ToneList;
import com.example.Marketten.dto.list.ToneExampleRequest;
import com.example.Marketten.dto.list.ToneExampleResponse;
import com.example.Marketten.repository.ToneExampleRepository;
import com.example.Marketten.repository.ToneListRepository;
import com.example.Marketten.util.FastApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToneExampleServiceImpl implements ToneExampleService {

    private final ToneExampleRepository toneExampleRepository;
    private final ToneListRepository toneListRepository;
    private final FastApiClient fastApiClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<ToneExampleResponse> getExamplesForTone(Long toneId) {
        return toneExampleRepository.findByTone_ToneId(toneId).stream()
                .map(e -> ToneExampleResponse.builder()
                        .toneExampleId(e.getToneExampleId())
                        .exampleText(e.getExampleText())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ToneExampleResponse createExample(Long toneId, ToneExampleRequest request) {
        ToneList tone = toneListRepository.findById(toneId)
                .orElseThrow(() -> new RuntimeException("Tone not found"));

        List<Double> embedding = fastApiClient.embedText(request.getExampleText());

        ToneExample example = toneExampleRepository.save(ToneExample.builder()
                .tone(tone)
                .exampleText(request.getExampleText())
                .embedding(writeEmbeddingAsJson(embedding))
                .build());

        return ToneExampleResponse.builder()
                .toneExampleId(example.getToneExampleId())
                .exampleText(example.getExampleText())
                .build();
    }

    @Override
    public void deleteExample(Long toneExampleId) {
        toneExampleRepository.deleteById(toneExampleId);
    }

    private String writeEmbeddingAsJson(List<Double> embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("임베딩 직렬화 실패: " + e.getMessage(), e);
        }
    }
}
