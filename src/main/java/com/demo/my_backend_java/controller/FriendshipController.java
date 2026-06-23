package com.demo.my_backend_java.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.entity.Friendship;
import com.demo.my_backend_java.entity.FriendshipStatus;
import com.demo.my_backend_java.repository.AppUserRepo;
import com.demo.my_backend_java.repository.FriendshipRepo;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    @Autowired
    private FriendshipRepo friendRepo;

    @Autowired
    private AppUserRepo userRepo;

    @PostMapping("/request")
    public ResponseEntity<?> requestFriend(@RequestBody Friendship friendship) {
        //check if the does the sender exist?
        if (friendship.getRequester() == null|| !userRepo.existsById(friendship.getRequester().getId())) {
            return ResponseEntity.badRequest().body("Error: Sender Does Not Exist");
        }

        //check if the receiver exist
        if (friendship.getReceiver() == null || !userRepo.existsById(friendship.getReceiver().getId())) {
            return ResponseEntity.badRequest().body("Error: Receiver Does Not Exist");
        }

        //send request to themself 
        if (friendship.getRequester().getId().equals(friendship.getReceiver().getId())) {
            return ResponseEntity.badRequest().body("Error: Cannot Send Request To Self");
        }

        friendship.setStatus(FriendshipStatus.PENDING);
        Friendship savedFriendship = friendRepo.save(friendship);
        return ResponseEntity.ok(savedFriendship);
    }

    @PutMapping("/accepted/{id}") 
    public ResponseEntity<Friendship> acceptFriendStatus(@PathVariable Long id) {
        Optional<Friendship> optionalFriendship = friendRepo.findById(id);

        if (optionalFriendship.isPresent()) {
            Friendship friendship = optionalFriendship.get();
            friendship.setStatus(FriendshipStatus.ACCEPTED);
            Friendship updateFriend = friendRepo.save(friendship);
            return ResponseEntity.ok(updateFriend);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/decline/{id}") 
    public ResponseEntity<Friendship> declineFriendStatus(@PathVariable Long id) {
        Optional<Friendship> optionalFriendship = friendRepo.findById(id);

        if (optionalFriendship.isPresent()) {
            Friendship friendship = optionalFriendship.get();
            friendship.setStatus(FriendshipStatus.REJECTED);
            Friendship updateFriend = friendRepo.save(friendship);
            return ResponseEntity.ok(updateFriend);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/list") 
    public ResponseEntity<List<Friendship>> getAllFriend(@PathVariable Long id) {
        List<Friendship> allFriend = friendRepo.findAll();

        List<Friendship> acceptedFriend = allFriend.stream()
            .filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
            .filter(f -> f.getReceiver().getId().equals(id) || f.getRequester().getId().equals(id))
            .toList();

        return ResponseEntity.ok(acceptedFriend);
    }
}
