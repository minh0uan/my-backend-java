package com.demo.my_backend_java.controller;

import java.util.List;

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
import com.demo.my_backend_java.service.FriendshipService;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    @Autowired
    private FriendshipService friendService;

    @PostMapping("/request")
    public ResponseEntity<?> requestFriend(@RequestBody Friendship friendship) {
        try {
            Friendship savedFriendship = friendService.requestFriend(friendship);
            return ResponseEntity.ok(savedFriendship); 
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/accepted/{id}") 
    public ResponseEntity<?> acceptFriendStatus(@PathVariable Long id) {
        try {
            Friendship updateFriend = friendService.acceptFriendStatus(id);
            return ResponseEntity.ok(updateFriend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/decline/{id}") 
    public ResponseEntity<?> declineFriendStatus(@PathVariable Long id) {
        try {
            Friendship updateFriend = friendService.declineFriendStatus(id);
            return ResponseEntity.ok(updateFriend);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/list/{id}") 
    public ResponseEntity<List<Friendship>> getAllFriend(@PathVariable Long id) {
        List<Friendship> acceptedFriend = friendService.getAllFriend(id);

        return ResponseEntity.ok(acceptedFriend);
    }
}
