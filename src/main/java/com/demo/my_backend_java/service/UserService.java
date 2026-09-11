package com.demo.my_backend_java.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.dto.LoginRequest;
import com.demo.my_backend_java.dto.RegisterRequest;
import com.demo.my_backend_java.dto.TokenResponse;
import com.demo.my_backend_java.dto.UserDto;
import com.demo.my_backend_java.entity.User;
import com.demo.my_backend_java.entity.UserRoleStatus;
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
            if (!contact.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new Exception("Error: Invalid Email Format");
            }

            if (userRepo.existsByEmail(contact)) {
                throw new Exception("Error: Email In Used");
            }

            user.setEmail(contact);
        } else {

            String cleanPhone = contact.replaceAll("[\\s\\-]", "");

            if (!cleanPhone.matches("^\\+?[0-9]{9,15}$")) {
                throw new Exception("Error: Invalid Phone Number Format");
            }


            if (userRepo.existsByPhone(cleanPhone)) {
                throw new Exception("Error: Phone Number In Used");
            }
            
            user.setPhone(cleanPhone);
        }

        if (password == null || password.trim().isEmpty()) {
            throw new Exception("Error: Provide A Secured Password");
        }

        //hash the password 12 times 
        //before saving it to the table
        user.setPassword(encoder.encode(password));

        User savedUSer = userRepo.save(user);

        return new UserDto(savedUSer.getId(), savedUSer.getUsername(), savedUSer.getRole());
    }

    public TokenResponse loginUser(LoginRequest loginRequest) throws Exception {
        
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.loginId(), loginRequest.password())
        );

        //pull out the loginId and check inside of UserDetails 
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String username = userDetails.getUsername();

        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        String token = jwtService.generateToken(username, role);
        
        return new TokenResponse(token);
    }

    public UserDto getUserProfile(Long id) throws Exception {
        User user =  userRepo.findById(id).orElseThrow(() -> new Exception("User not found"));

        return new UserDto(user.getId(), user.getUsername(), user.getRole());
    }

    public UserDto getCurrentUser(String username) throws Exception{    
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new Exception("User not found"));

        return new UserDto(user.getId(), user.getUsername(), user.getRole());
    }

    public void deleteUser(String username) throws Exception {

        if (!userRepo.existsByUsername(username)) {
            throw new RuntimeException("User Not Found");
        }
        
        userRepo.deleteByUsername(username);    
    }

    public UserDto upgradeToPremium(String username) throws Exception{
        User user = userRepo.findByUsername(username)
            .orElseThrow((() -> new Exception("User Not Found")));

        user.setRole(UserRoleStatus.PREMIUM);

        User savedUSer = userRepo.save(user);

        return new UserDto(savedUSer.getId(), savedUSer.getUsername(), savedUSer.getRole());
    }
}