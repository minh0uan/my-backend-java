package com.demo.my_backend_java.dto;

public record LoginRequest(
    String loginId, 
    String password
) {}
