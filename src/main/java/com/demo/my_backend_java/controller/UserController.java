package com.demo.my_backend_java.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.entity.AppUser;
import com.demo.my_backend_java.repository.AppUserRepo;

@RestController
public class UserController {
    
    @Autowired
    private AppUserRepo userRepo;

    @PostMapping("/register")
    public ResponseEntity<AppUser> registerUser(@RequestBody AppUser user) {
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
        return ResponseEntity.ok("Login endpoint");
    }
    
    
}
