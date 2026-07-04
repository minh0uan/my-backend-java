package com.demo.my_backend_java.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.dto.UserDto;
import com.demo.my_backend_java.entity.User;
import com.demo.my_backend_java.repository.UserRepo;

@Service
public class UserService {

    
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserDto registerUser(User user) throws Exception {
        
        if (userRepo.existsByEmail(user.getEmail())) {
            throw new Exception("Error: Email In Used");
        }
        if (userRepo.existsByPhone(user.getPhone())) {
            throw new Exception("Error: Phone Number In Used");
        }
        if (userRepo.existsByUsername(user.getUsername())) {
            throw new Exception("Error: User Name In Used");
        }

        //hash the password 12 times 
        //before saving it to the table
        user.setPassword(encoder.encode(user.getPassword()));

        User savedUSer = userRepo.save(user);

        return new UserDto(savedUSer.getId(), savedUSer.getUsername());
    }

    public UserDto getUserProfile(Long id) throws Exception {
        User user =  userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));

        return new UserDto(user.getId(), user.getUsername());
    }

    public String loginUser(User loginRequest) throws Exception {
        
        Optional<User> dataUserEmail = userRepo.findByEmail(loginRequest.getEmail());
        Optional<User> dataUserPhone = userRepo.findByPhone(loginRequest.getPhone());
        Optional<User> dataUsername = userRepo.findByUsername(loginRequest.getUsername());

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

    public void deleteUser(Long id) throws Exception {

        if (!userRepo.existsById(id)) {
            throw new RuntimeException("User Not Found");
        }
        
        userRepo.deleteById(id);
    }
}