package com.demo.my_backend_java.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.dto.FriendDto;
import com.demo.my_backend_java.dto.UserDto;
import com.demo.my_backend_java.entity.User;
import com.demo.my_backend_java.entity.Friendship;
import com.demo.my_backend_java.entity.FriendshipStatus;
import com.demo.my_backend_java.repository.UserRepo;
import com.demo.my_backend_java.repository.FriendshipRepo;

@Service
public class FriendshipService {
    
    private final FriendshipRepo friendRepo;

    private final UserRepo userRepo;

    private final StringRedisTemplate redisTemplate;

    public FriendshipService(FriendshipRepo friendshipRepo, UserRepo userRepo, StringRedisTemplate redisTemplate) {
        this.friendRepo = friendshipRepo;
        this.userRepo = userRepo;
        this.redisTemplate = redisTemplate;
    }

    public FriendDto requestFriend(Friendship friendship, String username) throws Exception {
        User requester = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User Not Found"));

        friendship.setRequester(requester); 

        if (friendship.getReceiver() == null || friendship.getReceiver().getUsername() == null) {
            throw new Exception("Error: Receiver Username Missing");
        }

        User receiver = userRepo.findByUsername(friendship.getReceiver().getUsername())
            .orElseThrow(() -> new RuntimeException("Error: The Receiver Does Not Exist"));

        friendship.setReceiver(receiver);

        Long requesterId = requester.getId();
        Long receiverId = friendship.getReceiver().getId();

        if (requesterId.equals(receiverId)) {
            throw new Exception("Error: Cannot Send Request To Self");
        }

        String blockKey = "cooldown_active:" + requesterId + ":" + receiverId;

        //redis check if the 
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockKey))) {
            throw new Exception("Error: Try Again Later!");
        }

        Optional<Friendship> friend = friendRepo.findFriendshipBetweenUsers(requesterId, receiverId);

        if (friend.isPresent() && friend.get().getStatus() == FriendshipStatus.ACCEPTED) {
            throw new Exception("Error: Already Friend");
        } else if (friend.isPresent() && friend.get().getStatus() == FriendshipStatus.PENDING) {
            throw new Exception("Answer/Wait For The Prior Request");
        } 

        friendship.setStatus(FriendshipStatus.PENDING);
        Friendship savedFriend = friendRepo.save(friendship);

        User savedRequester = savedFriend.getRequester();
        UserDto safeRequester = new UserDto(savedRequester.getId(), savedRequester.getUsername(), savedRequester.getRole());

        User savedReceiver = savedFriend.getReceiver();
        UserDto safeReceiver = new UserDto(savedReceiver.getId(), savedReceiver.getUsername(), savedReceiver.getRole());

        return new FriendDto(
            savedFriend.getId(),
            safeRequester,
            safeReceiver,
            savedFriend.getStatus()
        );
    }

    public List<FriendDto> getPendingRequests(String username) {
        User currentUser = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User Not Found"));

        List<Friendship> rawRequests = friendRepo.findPendingRequests(currentUser.getId());
        List<FriendDto> safeRequests = new ArrayList<>();

        for (Friendship request : rawRequests) {
            User requester = request.getRequester();
            UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername(), requester.getRole());

            User receiver = request.getReceiver();
            UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername(), receiver.getRole());

            safeRequests.add(new FriendDto(
                request.getId(), 
                safeRequester, 
                safeReceiver, 
                request.getStatus()
            ));
        }
        return safeRequests;
    }

    public FriendDto acceptFriendStatus(Long id) throws Exception {
        Friendship friendship = friendRepo.findById(id)
            .orElseThrow(() -> new Exception("Not found"));
        
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship savedFriend =  friendRepo.save(friendship);

        User requester = savedFriend.getRequester();
        UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername(), requester.getRole());

        User receiver = savedFriend.getReceiver();
        UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername(), receiver.getRole());

        return new FriendDto(
            savedFriend.getId(),
            safeRequester,
            safeReceiver,
            savedFriend.getStatus()
        );
    }

    public FriendDto declineFriendStatus(Long id, String username) throws Exception {
        Friendship friendship = friendRepo.findById(id)
            .orElseThrow(() -> new Exception("Reject User Not Found"));

        User receiver = userRepo.findByUsername(username)
            .orElseThrow(() -> new Exception("Not Found"));    
    
        User requester = friendship.getRequester();

        //create key 
        String countKey = "rejection_count:" + requester.getId() + ":" + receiver.getId();
        String blockKey = "cooldown_active:" + requester.getId() + ":" + receiver.getId();

        //find if key exist in redis 
        String currentCount = redisTemplate.opsForValue().get(countKey);
        int count;

        if (currentCount == null) {
            count = 0;
        } else {
            count = Integer.parseInt(currentCount);
        }

        count++;

        //apply penalty base on time rejected
        long penalty = switch (count) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 5;
            case 4 -> 12;
            default -> 24;
        };

        //save both to redis 
        redisTemplate.opsForValue().set(blockKey, "blocked", Duration.ofHours(penalty)); //create a block key and delete it after such hours 
        redisTemplate.opsForValue().set(countKey, String.valueOf(count), Duration.ofDays(30)); //if spammer leave the user untouch in 30 day redis reset

        friendRepo.delete(friendship);

        UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername(), requester.getRole());
        UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername(), receiver.getRole());

        return new FriendDto(
            friendship.getId(),
            safeRequester,
            safeReceiver,
            FriendshipStatus.REJECTED
        );
    }

    public List<FriendDto> getAllFriend(Long id) {
        List<Friendship> rawFriends = friendRepo.findAcceptedFriendships(id);

        List<FriendDto> safeFriends = new ArrayList<>();

        for (Friendship friend : rawFriends) {
            User requester = friend.getRequester();
            UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername(), requester.getRole());

            User receiver = friend.getReceiver();
            UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername(), receiver.getRole());

            FriendDto dto = new FriendDto(
                friend.getId(),
                safeRequester,
                safeReceiver,
                friend.getStatus()
            );

            safeFriends.add(dto);
        }
        
        return safeFriends;
    }

    //this service can be use for both pending and accepted relation ship 
    //so it can also be use to cancel user friend request   
    public void deleteFriend(Long idUser1, Long idUser2) {
        Optional<Friendship> friendship = friendRepo.findFriendshipBetweenUsers(idUser1, idUser2);

        if (friendship.isPresent()) {
            friendRepo.deleteById(friendship.get().getId());   
        } else {
            throw new RuntimeException("Friendship Does Not Exist");
        }
    }
}
