package com.demo.my_backend_java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.entity.AppUser;
import com.demo.my_backend_java.entity.Comment;
import com.demo.my_backend_java.entity.Like;
import com.demo.my_backend_java.entity.Mark;
import com.demo.my_backend_java.repository.AppUserRepo;
import com.demo.my_backend_java.repository.CommentRepo;
import com.demo.my_backend_java.repository.LikeRepo;
import com.demo.my_backend_java.repository.MarkRepo;

@Service
public class MarkService {
    
    @Autowired
    private MarkRepo markRepo;

    @Autowired 
    private AppUserRepo userRepo;

    @Autowired
    private CommentRepo cmtRepo;

    @Autowired
    private LikeRepo likeRepo;

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

    public Comment addComment(Long markId, Comment cmt) throws Exception {
        Mark targetMark = markRepo.findById(markId)
            .orElseThrow(() -> new Exception("Not Found"));

        if (cmt.getUser() == null || !userRepo.existsById(cmt.getUser().getId())) {
            throw new Exception("User Does Not Exist");
        }

        cmt.setMark(targetMark);
        targetMark.setCmtCount(targetMark.getCmtCount() + 1);    

        markRepo.save(targetMark);
        return cmtRepo.save(cmt);
    }
    
    public List<Comment> getAllComment(Long markId) {
        return cmtRepo.findByMarkId(markId);
    }

    public String likeMark(Long markId, Long userId) throws Exception {
        Mark mark = markRepo.findById(markId)
            .orElseThrow(() -> new Exception("Mark Not Found"));

        AppUser user = userRepo.findById(userId)
            .orElseThrow(() -> new Exception("User Not Found"));

        if (likeRepo.existsByMarkIdAndUserId(markId, userId)) {
            throw new Exception("User Already Like This Mark");
        }

        Like newLike = new Like();
        newLike.setMark(mark);
        newLike.setUser(user);
        likeRepo.save(newLike);

        mark.setLikeCount(mark.getLikeCount() + 1);
        markRepo.save(mark);

        return "Mark Liked";
    }

    public List<Mark> getAllMark(Long id) {
        return markRepo.findAll().stream()
            .filter(f -> f.getAppUser().getId().equals(id))
            .toList();
    }

    public String deleteMark(Long id) throws Exception {

        if (!markRepo.existsById(id)) {
            throw new Exception("Mark Not Found");
        }
        
        markRepo.deleteById(id);
        return "Mark Deletion Successful";
    }
}
