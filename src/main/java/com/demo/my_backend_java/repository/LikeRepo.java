package com.demo.my_backend_java.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.Like;

@Repository
public interface LikeRepo extends JpaRepository<Like, Long>{
    
}
