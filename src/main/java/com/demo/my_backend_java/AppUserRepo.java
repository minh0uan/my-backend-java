package com.demo.my_backend_java;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepo extends JpaRepository<AppUser, Long>{
    //repo for string boot to automatically update, insert, delete, ... 
} 