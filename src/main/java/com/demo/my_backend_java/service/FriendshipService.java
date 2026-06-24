package com.demo.my_backend_java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.entity.Friendship;
import com.demo.my_backend_java.entity.FriendshipStatus;
import com.demo.my_backend_java.repository.AppUserRepo;
import com.demo.my_backend_java.repository.FriendshipRepo;

@Service
public class FriendshipService {
    
    @Autowired
    private FriendshipRepo friendRepo;

    @Autowired
    private AppUserRepo userRepo;

    public Friendship requestFriend(Friendship friendship) throws Exception {
        if (friendship.getRequester() == null|| !userRepo.existsById(friendship.getRequester().getId())) {
            throw new Exception("Error: Sender Does Not Exist");
        }

        if (friendship.getReceiver() == null || !userRepo.existsById(friendship.getReceiver().getId())) {
            throw new Exception("Error: Receiver Does Not Exist");
        }

        Long requesterId = friendship.getRequester().getId();
        Long receiverId = friendship.getReceiver().getId();

        if (requesterId.equals(receiverId)) {
            throw new Exception("Error: Cannot Send Request To Self");
        }

        if (friendRepo.friendshipExists(requesterId, receiverId)) {
            throw new Exception("Error: Answer The Prior Request");
        }

        friendship.setStatus(FriendshipStatus.PENDING);
        return friendRepo.save(friendship);
    }

    public Friendship acceptFriendStatus(Long id) throws Exception {
        Friendship friendship = friendRepo.findById(id)
            .orElseThrow(() -> new Exception("Not found"));
        
        friendship.setStatus(FriendshipStatus.ACCEPTED);
        return friendRepo.save(friendship);
    }

    public Friendship declineFriendStatus(Long id) throws Exception {
        Friendship friendship = friendRepo.findById(id)
            .orElseThrow(() -> new Exception("Not found"));
        
        friendship.setStatus(FriendshipStatus.REJECTED);
        return friendRepo.save(friendship);
    }

    public List<Friendship> getAllFriend(Long id) {
        return friendRepo.findAll().stream()
            .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
            .filter(f -> f.getReceiver().getId().equals(id) || f.getRequester().getId().equals(id))
            .toList();
    }
}
