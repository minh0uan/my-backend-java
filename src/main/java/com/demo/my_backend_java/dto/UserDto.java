package com.demo.my_backend_java.dto;

import com.demo.my_backend_java.entity.UserRoleStatus;

public record UserDto(
    Long id,
    String username,
    UserRoleStatus role
) {
}
