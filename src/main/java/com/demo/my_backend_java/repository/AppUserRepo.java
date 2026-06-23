package com.demo.my_backend_java.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.AppUser;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser, Long>{
    //repo for string boot to automatically update, insert, delete, ... 

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByPhone(String phone);

    Optional<AppUser> findByUsername(String username);
} 