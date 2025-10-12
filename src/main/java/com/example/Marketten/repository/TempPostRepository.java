package com.example.Marketten.repository;

import com.example.Marketten.domain.TempPost;
import com.example.Marketten.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempPostRepository extends JpaRepository<TempPost, Long> {


    long countByUser(User user);
}