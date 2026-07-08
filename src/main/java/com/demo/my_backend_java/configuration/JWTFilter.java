package com.demo.my_backend_java.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.my_backend_java.service.JWTService;
import com.demo.my_backend_java.service.MyUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTFilter extends OncePerRequestFilter{

    private JWTService jwtService;

    private ApplicationContext applicationContext;

    public JWTFilter(JWTService jwtService, ApplicationContext applicationContext) {
        this.jwtService = jwtService;
        this.applicationContext = applicationContext;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        //Bearer token
        //skip the first 7 letter to get the token 
        String authHeader = request.getHeader("Authorization");         
        String token = null;
        String loginId = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            loginId = jwtService.extractUsername(token);
        }

        if (loginId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            
            if (jwtService.validateToken(token, loginId))
        }


    }
    
}
