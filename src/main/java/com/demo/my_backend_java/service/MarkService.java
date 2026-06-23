package com.demo.my_backend_java.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.entity.Mark;
import com.demo.my_backend_java.repository.MarkRepo;

@Service
public class MarkService {
    // Service for Mark entity, currently empty but can be implemented with business logic related to Mark
    @Autowired
    private MarkRepo markRepo;

    public Mark dropMark(Mark mark) throws Exception {

        if (markRepo.existsById(mark.getId())) { 
            throw new Exception("Error: Mark Already Exists");
        }
        if (markRepo.existByLatitude(mark.getLatitude()) && markRepo.existBylongitude(mark.getLongitude())) {
            throw new Exception("Error: Mark Already Exists At This Location");
        }

        return markRepo.save(mark);
    }

    public Mark getMarkInfo(Long id) throws Exception {
        return markRepo.findById(id).orElseThrow(() -> new Exception("Mark not found"));
    }
}

   
