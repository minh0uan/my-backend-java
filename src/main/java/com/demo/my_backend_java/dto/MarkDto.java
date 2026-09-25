package com.demo.my_backend_java.dto;

public record MarkDto(
    Long id,
    UserDto author, 
    String message,
    Double latitude,
    Double longitude,
    int viewCount,
    int likeCount,
    int cmtCount,
    boolean privacy,
    boolean hasPhoto
) {}