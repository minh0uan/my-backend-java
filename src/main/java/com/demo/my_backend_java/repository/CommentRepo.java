package com.demo.my_backend_java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.my_backend_java.entity.Comment;

public interface CommentRepo extends JpaRepository<Comment, Long>{
    List<Comment> findByMarkId(Long markId);
}
