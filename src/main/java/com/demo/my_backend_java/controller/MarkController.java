package com.demo.my_backend_java.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.dto.CommentDto;
import com.demo.my_backend_java.dto.MarkDto;
import com.demo.my_backend_java.entity.Comment;
import com.demo.my_backend_java.entity.Mark;
import com.demo.my_backend_java.service.MarkService;

@RestController
@RequestMapping("/api/marks")
public class MarkController {
    
    private final MarkService markService;

    public MarkController(MarkService markService) {
        this.markService = markService;
    }

    @PostMapping("/drop")
    public ResponseEntity<?> dropMark(@RequestBody Mark mark)  {
        try {
            MarkDto savedMark = markService.dropMark(mark);
            return ResponseEntity.ok(savedMark);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<MarkDto>> getNearbyMarks(@RequestParam Double lat, @RequestParam Double lng) {
        
        List<MarkDto> nearby = markService.getNearbyMarks(lat, lng);

        return ResponseEntity.ok(nearby);
    }

    @PostMapping("/{id}/view") 
    public ResponseEntity<?> addView(@PathVariable Long id) {
        try {
            MarkDto markView = markService.addView(id);
            return ResponseEntity.ok(markView);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable("id") Long markId,@RequestBody Comment cmt) {
        try {
            CommentDto savedComment = markService.addComment(markId, cmt);
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/list-comment")
    public ResponseEntity<List<CommentDto>> getAllComment(@PathVariable("id") Long markId) {
        List<CommentDto> allCmt = markService.getAllComment(markId);
        
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
    public ResponseEntity<List<MarkDto>> getAllMark(@PathVariable Long id) {
        List<MarkDto> marks = markService.getAllMark(id);
        return ResponseEntity.ok(marks);
    }

    @DeleteMapping("/delete/like/{id}")
    public ResponseEntity<String> deleteLike(@PathVariable Long id) throws Exception {
        try {
            markService.deleteLike(id);
            return ResponseEntity.ok("Like Deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Could Not Delete Try Again Later");
        }
    }

    @DeleteMapping("/delete/comment/{id}")
    public ResponseEntity<String> deleteCmt(@PathVariable Long id) throws Exception {
        try {
            markService.deleteCmt(id);
            return ResponseEntity.ok("Comment Deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Could Not Delete Try Again Later");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteMark(@PathVariable Long id) throws Exception {
        try {
            markService.deleteMark(id);
            return ResponseEntity.ok("Mark Deleted");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Could Not Delete Try Again Later");
        }
    }
}
