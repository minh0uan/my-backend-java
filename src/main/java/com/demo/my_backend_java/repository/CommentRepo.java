package com.demo.my_backend_java.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.Comment;

@Repository
public interface CommentRepo extends JpaRepository<Comment, Long>{
    
}
