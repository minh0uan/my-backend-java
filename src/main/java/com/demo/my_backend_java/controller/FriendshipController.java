package com.demo.my_backend_java.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.dto.FriendDto;
import com.demo.my_backend_java.entity.Friendship;
import com.demo.my_backend_java.service.FriendshipService;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final FriendshipService friendService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendService = friendshipService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestFriend(@RequestBody Friendship friendship, Principal principal) {
        try {
            FriendDto savedFriendship = friendService.requestFriend(friendship, principal.getName());
            return ResponseEntity.ok(savedFriendship); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/pending") 
    public ResponseEntity<?> getPendingRequests(Principal principal) {
        try {
            List<FriendDto> pendingRequests = friendService.getPendingRequests(principal.getName());
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/accept/{id}") 
    public ResponseEntity<?> acceptFriendStatus(@PathVariable Long id) {
        try {
            FriendDto updateFriend = friendService.acceptFriendStatus(id);
            return ResponseEntity.ok(updateFriend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/decline/{id}") 
    public ResponseEntity<?> declineFriendStatus(@PathVariable Long id) {
        try {
            FriendDto updateFriend = friendService.declineFriendStatus(id);
            return ResponseEntity.ok(updateFriend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list/{id}") 
    public ResponseEntity<List<FriendDto>> getAllFriend(@PathVariable Long id) {
        List<FriendDto> acceptedFriend = friendService.getAllFriend(id);

        return ResponseEntity.ok(acceptedFriend);
    }

    @DeleteMapping("/delete-friend/{id}")
    public ResponseEntity<String> deleteFriend(@RequestParam Long idUser1, @PathVariable("id") Long idUser2) {
        try {
            friendService.deleteFriend(idUser1, idUser2);
            return ResponseEntity.ok("Friend Deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Could Not Delete Try Again Later");
        }
    }
}
