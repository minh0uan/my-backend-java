package com.demo.my_backend_java.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.demo.my_backend_java.entity.AppUser;
import com.demo.my_backend_java.repository.AppUserRepo;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private AppUserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AppUser user) {

        if (userRepo.existByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email In Used");
        }

        if (userRepo.existByPhoneNum(user.getPhone())) {
            return ResponseEntity.badRequest().body("Error: Phone Number In Used");
        }

        AppUser savedUser = userRepo.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserProfile(@PathVariable Long id) {
        Optional<AppUser> user = userRepo.findById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody AppUser loginRequest) {
        Optional<AppUser> dataUserEmail = userRepo.findByEmail(loginRequest.getEmail());
        Optional<AppUser> dataUserPhone = userRepo.findByPhone(loginRequest.getPhone());
        Optional<AppUser> dataUsername = userRepo.findByUserName(loginRequest.getUsername());

        if (dataUserEmail.isPresent() && dataUserEmail.get().getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.ok("Login Successful By Email");
        }
        
        if (dataUserPhone.isPresent() && dataUserPhone.get().getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.ok("Login Successful By Phone Number");
        }
        
        if (dataUsername.isPresent() && dataUsername.get().getPassword().equals(loginRequest.getPassword())) {
            return (ResponseEntity.ok("Login Successful By Username"));
        } 

        return ResponseEntity.status(401).body("Error: Invalid");
        
    }   
    
}
