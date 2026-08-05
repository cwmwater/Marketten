package com.example.Marketten.service;

import com.example.Marketten.domain.ToneList;
import com.example.Marketten.dto.list.ToneListRequest;
import com.example.Marketten.dto.list.ToneListResponse;
import com.example.Marketten.repository.ToneListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ToneListServiceImpl implements ToneListService {

    private final ToneListRepository toneListRepository;

    @Override
    public List<ToneListResponse> getAllTones() {
        // 사용자용 - 전체 톤 리스트 조회
        return toneListRepository.findAll().stream()
                .map(t -> ToneListResponse.builder()
                        .toneId(t.getToneId())
                        .toneName(t.getToneName())
                        .tonePreview(t.getTonePreview())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ToneListResponse createTone(ToneListRequest request) {
        // 관리자용 - 새로운 톤 추가
        ToneList tone = toneListRepository.save(ToneList.builder()
                .toneName(request.getToneName())
                .tonePreview(request.getTonePreview())
                .build());

        return ToneListResponse.builder()
                .toneId(tone.getToneId())
                .toneName(tone.getToneName())
                .tonePreview(tone.getTonePreview())
                .build();
    }

    @Override
    public ToneListResponse updateTone(Long toneId, ToneListRequest request) {
        // 관리자용 - 기존 톤 수정
        ToneList tone = toneListRepository.findById(toneId)
                .orElseThrow(() -> new RuntimeException("Tone not found"));

        if (request.getToneName() != null) {
            tone.setToneName(request.getToneName());
        }
        if (request.getTonePreview() != null) {
            tone.setTonePreview(request.getTonePreview());
        }
        toneListRepository.save(tone);

        return ToneListResponse.builder()
                .toneId(tone.getToneId())
                .toneName(tone.getToneName())
                .tonePreview(tone.getTonePreview())
                .build();
    }

    @Override
    public void deleteTone(Long toneId) {
        // 관리자용 - 톤 삭제
        toneListRepository.deleteById(toneId);
    }
}

