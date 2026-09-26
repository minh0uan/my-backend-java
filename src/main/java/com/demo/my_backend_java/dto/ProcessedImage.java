package com.demo.my_backend_java.dto;

public record ProcessedImage(
    byte[] data,
    String contentType,
    long sizeBytes,
    int width,
    int height
) {}
