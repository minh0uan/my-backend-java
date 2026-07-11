package com.demo.my_backend_java.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.dto.LoginRequest;
import com.demo.my_backend_java.dto.RegisterRequest;
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

    public UserDto registerUser(RegisterRequest registerRequest) throws Exception {
        User user = new User();

        String username = registerRequest.username();
        String contact = registerRequest.contact();
        String password = registerRequest.password();

        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Error: Enter A User Name");
        }

        if ( userRepo.existsByUsername(username)) {
            throw new Exception("Error: User Name In Used");
        }

        user.setUsername(username);

        if (contact == null || contact.trim().isEmpty()) {
            throw new Exception("Error: Provide An Email Or Phone Number");
        }

        contact = contact.trim();
        if (contact.contains("@")) {
            if (userRepo.existsByEmail(contact)) {
                throw new Exception("Error: Email In Used");
            }
            user.setEmail(contact);
        } else if (contact.matches("^[0-9\\+\\-\\s]+$")) {
            if (userRepo.existsByPhone(contact)) {
                throw new Exception("Error: Phone Number In Used");
            }
            user.setPhone(contact);
        } else {
            throw new Exception("Error: Invalid, Please Enter A Valid Email Or Phone Number");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Error: Provide A Secured Password");
        }

        //hash the password 12 times 
        //before saving it to the table
        user.setPassword(encoder.encode(password));

        User savedUSer = userRepo.save(user);

        return new UserDto(savedUSer.getId(), savedUSer.getUsername());
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

    public UserDto getUserProfile(Long id) throws Exception {
        User user =  userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));

        return new UserDto(user.getId(), user.getUsername());
    }

    public void deleteUser(Long id) throws Exception {

        if (!userRepo.existsById(id)) {
            throw new RuntimeException("User Not Found");
        }
        
        userRepo.deleteById(id);
    }
}