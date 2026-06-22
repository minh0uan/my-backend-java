package com.demo.my_backend_java.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.Friendship;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long>{
    
}
