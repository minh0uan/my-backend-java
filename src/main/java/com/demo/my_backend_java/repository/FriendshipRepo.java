package com.demo.my_backend_java.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.Friendship;

@Repository
public interface FriendshipRepo extends JpaRepository<Friendship, Long> {
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE (f.requester.id = :user1 AND f.receiver.id = :user2) OR (f.requester.id = :user2 AND f.receiver.id = :user1)")
    boolean friendshipExists(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :user1 AND f.receiver.id = :user2) OR (f.requester.id = :user2 AND f.receiver.id = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);
}
