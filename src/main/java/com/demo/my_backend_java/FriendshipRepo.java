package com.demo.my_backend_java;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepo extends JpaRepository<Friendship, Long>{
    
}
