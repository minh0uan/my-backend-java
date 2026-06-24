package com.demo.my_backend_java.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.entity.AppUser;
import com.demo.my_backend_java.repository.AppUserRepo;

@Service
public class UserService {

    @Autowired
    private AppUserRepo userRepo;

    public AppUser registerUser(AppUser user) throws Exception {
        
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new Exception("Error: Email In Used");
        }
        if (userRepo.existsByPhone(user.getPhone())) {
            throw new Exception("Error: Phone Number In Used");
        }
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new Exception("Error: User Name In Used");
        }
        return userRepo.save(user);
    }

    public AppUser getUserProfile(Long id) throws Exception {
        return userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));
    }

    public String loginUser(AppUser loginRequest) throws Exception {
        
        Optional<AppUser> dataUserEmail = userRepo.findByEmail(loginRequest.getEmail());
        Optional<AppUser> dataUserPhone = userRepo.findByPhone(loginRequest.getPhone());
        Optional<AppUser> dataUsername = userRepo.findByUsername(loginRequest.getUsername());

        if (dataUserEmail.isPresent() && dataUserEmail.get().getPassword().equals(loginRequest.getPassword())) {
            return "Login Successful By Email";
        }
        
        if (dataUserPhone.isPresent() && dataUserPhone.get().getPassword().equals(loginRequest.getPassword())) {
            return "Login Successful By Phone Number";
        }
        
        if (dataUsername.isPresent() && dataUsername.get().getPassword().equals(loginRequest.getPassword())) {
            return "Login Successful By Username";
        } 

        throw new Exception("Error: Invalid credentials");
    }
}