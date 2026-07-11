package com.demo.my_backend_java.dto;

public record RegisterRequest(
    String username,
    String contact,
    String password
) {}
