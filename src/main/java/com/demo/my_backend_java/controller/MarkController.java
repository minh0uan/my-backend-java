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
import com.demo.my_backend_java.repository.AppUserRepo;
import com.demo.my_backend_java.repository.MarkRepo;

@RestController
@RequestMapping("api/marks")
public class MarkController {
    
    @Autowired
    private MarkRepo markRepo;

    @Autowired
    private AppUserRepo userRepo;

    @PostMapping("/drop")
    public ResponseEntity<?> dropMark(@RequestBody Mark mark)  {
        if (mark.getAppUser() == null || !userRepo.existsById(mark.getAppUser().getId())) { 
            return ResponseEntity.badRequest().body("User Does Not Exist");
        }

        Mark savedMark = markRepo.save(mark);
        return ResponseEntity.ok(savedMark);
    }
    
    @GetMapping("/all-marks/{id}") 
    private List<Mark> getAllMark(@PathVariable Long id) {

        List<Mark> allMark = markRepo.findAll();

        List<Mark> validMark = allMark.stream()
            .filter(f -> f.getAppUser().getId().equals(id))
            .toList();

        return validMark;
    }
    

}
