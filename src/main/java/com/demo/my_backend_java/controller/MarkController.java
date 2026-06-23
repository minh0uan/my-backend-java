package com.demo.my_backend_java.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.my_backend_java.entity.Mark;
import com.demo.my_backend_java.repository.MarkRepo;

@RestController
@RequestMapping("api/marks")
public class MarkController {
    
    @Autowired
    private MarkRepo markService; // Injecting Service instead of Repo


    @PostMapping("/drop")
    public ResponseEntity<?> dropMark(@RequestBody Mark mark)  {
        try {
            Mark savedMark = markService.save(mark);
            return ResponseEntity.ok(savedMark);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/all-marks/{id}") 
    private List<Mark> getAllMark(@PathVariable Long id) {
        try {
            List<Mark> allMark = markService.findAll();

            List<Mark> validMark = allMark.stream()
            .filter(f -> f.getAppUser().getId().equals(id)).toList();

        return validMark;
            
        } catch (Exception e) {
            return (List<Mark>) ResponseEntity.status(404).body(e.getMessage());
        }
    }
    

}
