package com.demo.my_backend_java.dto;

public record MarkDto(
    Long id,
    String username,
    String message,
    int view_count,
    int like_count,
    int cmt_count,
    int privacy
) {
} 
    

