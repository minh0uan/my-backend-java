package com.demo.my_backend_java.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.my_backend_java.entity.User;

public interface UserRepo extends JpaRepository<User, Long>{
    //repo for string boot to automatically update, insert, delete, ... 

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByUsername(String username);
} 