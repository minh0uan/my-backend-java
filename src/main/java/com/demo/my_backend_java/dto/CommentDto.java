package com.demo.my_backend_java.dto;

public record CommentDto( 
    Long id,
    UserDto commenter,
    MarkDto commentedMark,
    String contain
) {
}
