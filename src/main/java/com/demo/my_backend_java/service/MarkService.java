package com.demo.my_backend_java.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demo.my_backend_java.dto.MarkDto;
import com.demo.my_backend_java.dto.UserDto;
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

    public MarkDto dropMark(Mark mark) throws Exception {
        if (mark.getAppUser() == null || !userRepo.existsById(mark.getAppUser().getId())) { 
            throw new Exception("User Does Not Exist");
        }

        Mark savedMark = markRepo.save(mark);

        AppUser author = savedMark.getAppUser();
        UserDto safeAuthor = new UserDto(author.getId(), author.getUsername());

        return new MarkDto(
            savedMark.getId(),
            safeAuthor,
            savedMark.getMessage(),
            savedMark.getLatitude(),
            savedMark.getLongitude(),
            savedMark.getView(),
            savedMark.getLikeCount(),
            savedMark.getCmtCount(),
            savedMark.isPrivacy()
        );
    }

    public List<MarkDto> getNearbyMarks(Double lat, Double lng) {
        List<Mark> rawMarks = markRepo.findNearbyMarks(lat, lng);

        List<MarkDto> safeMarks = new ArrayList<>();

        for (Mark mark : rawMarks) {
            AppUser author = mark.getAppUser();
            UserDto safeAuthor = new UserDto(author.getId(), author.getUsername());

            MarkDto dto = new MarkDto(
                mark.getId(),
                safeAuthor,
                mark.getMessage(),
                mark.getLatitude(),
                mark.getLongitude(),
                mark.getView(),
                mark.getLikeCount(),
                mark.getCmtCount(),
                mark.isPrivacy()
            );

            safeMarks.add(dto);
        }

        return safeMarks;
    }

    public MarkDto addView(Long id) throws Exception {
        Mark mark = markRepo.findById(id)
            .orElseThrow(() -> new Exception("Mark not found"));
        
        mark.setView(mark.getView() + 1);
        Mark savedMark =  markRepo.save(mark);

        AppUser author = savedMark.getAppUser();
        UserDto safeAuthor = new UserDto(author.getId(), author.getUsername());

        return new MarkDto(
            savedMark.getId(),
            safeAuthor,
            savedMark.getMessage(),
            savedMark.getLatitude(),
            savedMark.getLongitude(),
            savedMark.getView(),
            savedMark.getLikeCount(),
            savedMark.getCmtCount(),
            savedMark.isPrivacy()
        );
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

    public List<MarkDto> getAllMark(Long id) {
        List<Mark> rawMarks = markRepo.findAll().stream()
            .filter(f -> f.getAppUser().getId().equals(id))
            .toList();

        List<MarkDto> safeMarks = new ArrayList<>();

        for (Mark mark : rawMarks) {
            AppUser author = mark.getAppUser();
            UserDto safeAuthor = new UserDto(author.getId(), author.getUsername());

            MarkDto dto = new MarkDto(
                mark.getId(),
                safeAuthor,
                mark.getMessage(),
                mark.getLatitude(),
                mark.getLongitude(),
                mark.getView(),
                mark.getLikeCount(),
                mark.getCmtCount(),
                mark.isPrivacy()
            );

            safeMarks.add(dto);
        }

        return safeMarks;
    }

    public void deleteLike(Long id) throws Exception {
        if(!likeRepo.existsById(id)) {
            throw new RuntimeException("Mark Not Found");
        }

        likeRepo.deleteById(id);
    }

    public void deleteCmt(Long id) throws Exception {
        if(!cmtRepo.existsById(id)) {
            throw new RuntimeException("Mark Not Found"); 
        }

        cmtRepo.deleteById(id); 
    }

    public void deleteMark(Long id) throws Exception {

        if (!markRepo.existsById(id)) {
            throw new RuntimeException("Mark Not Found");
        }
        
        markRepo.deleteById(id);
    }
}
