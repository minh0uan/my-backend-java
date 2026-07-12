package com.demo.my_backend_java.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.dto.LoginRequest;
import com.demo.my_backend_java.dto.RegisterRequest;
import com.demo.my_backend_java.dto.TokenResponse;
import com.demo.my_backend_java.dto.UserDto;
import com.demo.my_backend_java.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService; // Injecting Service instead of Repo

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            UserDto savedUser = userService.registerUser(registerRequest);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            TokenResponse successMessage = userService.loginUser(loginRequest);
            return ResponseEntity.ok(successMessage);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Error: Invalid Credentials");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Something Went Wrong");
        }
    }   

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        try {
            UserDto user = userService.getUserProfile(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) throws Exception {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Account Delete Sucessful");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Could Not Delete User");
        }
    }
}