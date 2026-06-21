package com.demo.my_backend_java;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendShipRepo extends JpaRepository<Friendship, Long>{
    
}
