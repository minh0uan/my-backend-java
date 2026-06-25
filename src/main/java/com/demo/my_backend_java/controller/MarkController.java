package com.demo.my_backend_java.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.entity.Comment;
import com.demo.my_backend_java.entity.Mark;
import com.demo.my_backend_java.service.MarkService;

@RestController
@RequestMapping("/api/marks")
public class MarkController {
    
    @Autowired
    private MarkService markService;


    @PostMapping("/drop")
    public ResponseEntity<?> dropMark(@RequestBody Mark mark)  {
        try {
            Mark savedMark = markService.dropMark(mark);
            return ResponseEntity.ok(savedMark);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<Mark>> getNearbyMarks(@RequestParam Double lat, @RequestParam Double lng) {
        
        List<Mark> nearby = markService.getNearbyMarks(lat, lng);

        return ResponseEntity.ok(nearby);
    }

    @PostMapping("/{id}/view") 
    public ResponseEntity<?> addView(@PathVariable Long id) {
        try {
            Mark markView = markService.addView(id);
            return ResponseEntity.ok(markView);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable("id") Long markId,@RequestBody Comment cmt) {
        try {
            Comment savedComment = markService.addComment(markId, cmt);
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/list-comment")
    public ResponseEntity<List<Comment>> getAllComment(@PathVariable("id") Long markId) {
        List<Comment> allCmt = markService.getAllComment(markId);
        
        return ResponseEntity.ok(allCmt);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<String> likeMark(@PathVariable("id") Long markId,@RequestParam Long userId) {
        try {
            String result = markService.likeMark(markId, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
    @GetMapping("/all-marks/{id}") 
    public ResponseEntity<List<Mark>> getAllMark(@PathVariable Long id) {
        List<Mark> marks = markService.getAllMark(id);
        return ResponseEntity.ok(marks);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteMark(@PathVariable Long id) throws Exception {
        try {
            return markService.deleteMark(id);
        } catch (Exception e) {
            return e.getMessage();
        } 
    }
}
