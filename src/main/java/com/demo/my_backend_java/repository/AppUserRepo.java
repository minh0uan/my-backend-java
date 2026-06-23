package com.demo.my_backend_java.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.AppUser;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser, Long>{
    //repo for string boot to automatically update, insert, delete, ... 

    boolean existByEmail(String email);

    boolean existByPhoneNum(String phone);
} 