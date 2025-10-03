package com.example.Marketten.repository;

import com.example.Marketten.domain.KeywordList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<KeywordList,Long> {
}
