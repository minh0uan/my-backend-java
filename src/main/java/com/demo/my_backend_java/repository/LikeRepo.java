package com.demo.my_backend_java.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.my_backend_java.entity.Like;

public interface LikeRepo extends JpaRepository<Like, Long>{
    boolean existsByMarkIdAndUserId(Long markId, Long userId);
}
