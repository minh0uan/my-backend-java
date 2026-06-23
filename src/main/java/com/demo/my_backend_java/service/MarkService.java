package com.demo.my_backend_java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.entity.Mark;
import com.demo.my_backend_java.repository.AppUserRepo;
import com.demo.my_backend_java.repository.MarkRepo;

@Service
public class MarkService {
    
    @Autowired
    private MarkRepo markRepo;

    @Autowired 
    private AppUserRepo userRepo;

    public Mark dropMark(Mark mark) throws Exception {
        if (mark.getAppUser() == null || !userRepo.existsById(mark.getAppUser().getId())) { 
            throw new Exception("User Does Not Exist");
        }

        return markRepo.save(mark);
    }

    public List<Mark> getNearbyMarks(Double lat, Double lng) {
        return markRepo.findNearbyMarks(lat, lng);
    }

    public Mark addView(Long id) throws Exception {
        Mark mark = markRepo.findById(id)
            .orElseThrow(() -> new Exception("Mark not found"));
        
        mark.setView(mark.getView() + 1);
        return markRepo.save(mark);
    }

    public List<Mark> getAllMark(Long id) {
        return markRepo.findAll().stream()
            .filter(f -> f.getAppUser().getId().equals(id))
            .toList();
    }
}
