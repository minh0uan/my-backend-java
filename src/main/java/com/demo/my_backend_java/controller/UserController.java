package com.demo.my_backend_java.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    //admin only
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        try {
            UserDto user = userService.getUserProfile(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {
        try {
            UserDto user = userService.getCurrentUser(principal.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/delete/{username}")
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')") //allow both user (the owner of the account) and admin 
    public ResponseEntity<String> deleteUser(@PathVariable String username) throws Exception {
        try {
            userService.deleteUser(username);
            return ResponseEntity.ok("Account Delete Sucessful");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Could Not Delete User");
        }
    }
}