package com.demo.my_backend_java.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.entity.User;
import com.demo.my_backend_java.entity.UserPrinciple;
import com.demo.my_backend_java.repository.UserRepo;

@Service
public class MyUserDetailsService implements UserDetailsService{
    
    private final UserRepo userRepo;

    public MyUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException{
        User user = userRepo.findByUsernameOrEmailOrPhone(loginInput, loginInput, loginInput)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserPrinciple(user);
    }
}
