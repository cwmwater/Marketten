package com.example.Marketten.repository;

import com.example.Marketten.domain.ToneList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToneListRepository extends JpaRepository<ToneList,Long> {
    Optional<ToneList> findByToneName(String toneName);
}
