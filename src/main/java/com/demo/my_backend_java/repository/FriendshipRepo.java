package com.demo.my_backend_java.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.my_backend_java.entity.Friendship;
import com.demo.my_backend_java.entity.FriendshipStatus;

public interface FriendshipRepo extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :user1 AND f.receiver.id = :user2) OR (f.requester.id = :user2 AND f.receiver.id = :user1) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE (f.requester.id = :user1 AND f.receiver.id = :user2) OR (f.requester.id = :user2 AND f.receiver.id = :user1)")
    boolean friendshipExists(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :user1 AND f.receiver.id = :user2) OR (f.requester.id = :user2 AND f.receiver.id = :user1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT COUNT(f) > 0 FROM Friend f " +
           "WHERE ((f.requester.username = :user1 AND f.receiver.username = :user2) " +
           "   OR (f.requester.username = :user2 AND f.receiver.username = :user1)) " +
           "AND f.status = :status")
    boolean existByUsernameAndStatus(@Param("user1") String user1, @Param("user2") String user2, @Param("status") FriendshipStatus status);

    default boolean isAcceptedFriend(String user1, String user2) {
        return existByUsernameAndStatus(user1, user2, FriendshipStatus.ACCEPTED);
    }
}
