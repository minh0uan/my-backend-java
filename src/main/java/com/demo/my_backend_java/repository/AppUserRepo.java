package com.demo.my_backend_java.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.AppUser;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser, Long>{
    //repo for string boot to automatically update, insert, delete, ... 

    boolean existByEmail(String email);

    boolean existByPhoneNum(String phone);

    boolean existByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByPhone(String phone);

    Optional<AppUser> findByUserName(String username);
} 