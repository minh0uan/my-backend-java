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
        return ResponseEntity.ok("Login endpoint");
    }
    
    
}
