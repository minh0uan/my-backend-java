package com.demo.my_backend_java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.entity.AppUser;
import com.demo.my_backend_java.repository.AppUserRepo;

@RestController
public class UserController {
    
    @Autowired
    private AppUserRepo userRepo;


    @PostMapping("/addPerson")
    public void createUser(@RequestBody AppUser user) {
        userRepo.save(user);
    }
}
