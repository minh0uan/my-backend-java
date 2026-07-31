package com.demo.my_backend_java.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public FriendshipService(FriendshipRepo friendshipRepo, UserRepo userRepo) {
        this.friendRepo = friendshipRepo;
        this.userRepo = userRepo;
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

        Optional<Friendship> friend = friendRepo.findFriendshipBetweenUsers(requesterId, receiverId);

        if (friend.isPresent() && friend.get().getStatus() == FriendshipStatus.ACCEPTED) {
            throw new Exception("Error: Already Friend");
        } else if (friend.isPresent() && friend.get().getStatus() == FriendshipStatus.PENDING) {
            throw new Exception("Answer/Wait For The Prior Request");
        } 

        friendship.setStatus(FriendshipStatus.PENDING);
        Friendship savedFriend = friendRepo.save(friendship);

        User savedRequester = savedFriend.getRequester();
        UserDto safeRequester = new UserDto(savedRequester.getId(), savedRequester.getUsername());

        User savedReceiver = savedFriend.getReceiver();
        UserDto safeReceiver = new UserDto(savedReceiver.getId(), savedReceiver.getUsername());

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
            UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername());

            User receiver = request.getReceiver();
            UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername());

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
        UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername());

        User receiver = savedFriend.getReceiver();
        UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername());

        return new FriendDto(
            savedFriend.getId(),
            safeRequester,
            safeReceiver,
            savedFriend.getStatus()
        );
    }

    public FriendDto declineFriendStatus(Long id) throws Exception {
        Friendship friendship = friendRepo.findById(id)
            .orElseThrow(() -> new Exception("Not found"));
        
        friendship.setStatus(FriendshipStatus.REJECTED);
        Friendship savedFriend =  friendRepo.save(friendship);

        User requester = savedFriend.getRequester();
        UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername());

        User receiver = savedFriend.getReceiver();
        UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername());

        return new FriendDto(
            savedFriend.getId(),
            safeRequester,
            safeReceiver,
            savedFriend.getStatus()
        );
    }

    public List<FriendDto> getAllFriend(Long id) {
        List<Friendship> rawFriends = friendRepo.findAcceptedFriendships(id);

        List<FriendDto> safeFriends = new ArrayList<>();

        for (Friendship friend : rawFriends) {
            User requester = friend.getRequester();
            UserDto safeRequester = new UserDto(requester.getId(), requester.getUsername());

            User receiver = friend.getReceiver();
            UserDto safeReceiver = new UserDto(receiver.getId(), receiver.getUsername());

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

        if (friendship.isPresent() && friendship.get().getStatus() != FriendshipStatus.REJECTED) {
            friendRepo.deleteById(friendship.get().getId());   
        } else {
            throw new RuntimeException("Friendship Does Not Exist");
        }
    }
}
