package com.demo.my_backend_java.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.dto.LoginRequest;
import com.demo.my_backend_java.dto.UserDto;
import com.demo.my_backend_java.entity.User;
import com.demo.my_backend_java.repository.UserRepo;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserService(UserRepo userRepo, AuthenticationManager authenticationManager, JWTService jwtService) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserDto registerUser(User user) throws Exception {

        if (user.getEmail() == null && user.getPhone() == null) {
            throw new Exception("Error: Provide An Email Or Phone Number");
        }

        if (user.getEmail() != null && userRepo.existsByEmail(user.getEmail())) {
            throw new Exception("Error: Email In Used");
        }

        if (user.getPhone() != null && userRepo.existsByPhone(user.getPhone())) {
            throw new Exception("Error: Phone Number In Used");
        }

        if (user.getUsername() == null) {
            throw new Exception("Error: Enter A User Name");
        }

        if (user.getUsername() != null && userRepo.existsByUsername(user.getUsername())) {
            throw new Exception("Error: User Name In Used");
        }

        if (user.getPassword() == null){
            throw new Exception("Error: Provided An Password");
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

    public String loginUser(LoginRequest loginRequest) throws Exception {
        
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.loginId(), loginRequest.password())
        );

        if (auth.isAuthenticated()) {
            return jwtService.generateToken(loginRequest.loginId());
        }
        
        return "Invalid Credentials";
    }


    public void deleteUser(Long id) throws Exception {

        if (!userRepo.existsById(id)) {
            throw new RuntimeException("User Not Found");
        }
        
        userRepo.deleteById(id);
    }
}