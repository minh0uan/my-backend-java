package com.demo.my_backend_java.dto;

import com.demo.my_backend_java.entity.FriendshipStatus;

public record FriendDto(
    Long id,
    UserDto requester, 
    UserDto receiver,  
    FriendshipStatus status
) {
}
   