package com.example.Marketten.repository;

import com.example.Marketten.domain.ToneExample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToneExampleRepository extends JpaRepository<ToneExample, Long> {
    List<ToneExample> findByTone_ToneId(Long toneId);
}
