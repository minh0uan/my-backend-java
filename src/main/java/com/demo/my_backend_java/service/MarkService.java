package com.demo.my_backend_java.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.demo.my_backend_java.dto.CommentDto;
import com.demo.my_backend_java.dto.MarkDto;
import com.demo.my_backend_java.dto.UserDto;
import com.demo.my_backend_java.entity.User;
import com.demo.my_backend_java.entity.Comment;
import com.demo.my_backend_java.entity.Like;
import com.demo.my_backend_java.entity.Mark;
import com.demo.my_backend_java.repository.UserRepo;
import com.demo.my_backend_java.repository.CommentRepo;
import com.demo.my_backend_java.repository.FriendshipRepo;
import com.demo.my_backend_java.repository.LikeRepo;
import com.demo.my_backend_java.repository.MarkRepo;

@Service
public class MarkService {
    
    private final MarkRepo markRepo;

    private final UserRepo userRepo;

    private final CommentRepo cmtRepo;
    
    private final LikeRepo likeRepo;

    private final FriendshipRepo friendshipRepo;

    public MarkService (MarkRepo markRepo, UserRepo userRepo, CommentRepo cmtRepo, LikeRepo likeRepo, FriendshipRepo friendshipRepo) {
        this.markRepo = markRepo;
        this.userRepo = userRepo;
        this.cmtRepo = cmtRepo;
        this.likeRepo = likeRepo;
        this.friendshipRepo = friendshipRepo;
    }

    public MarkDto createMark(Mark mark, String username) throws Exception {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new Exception("User Does Not Exist"));

        mark.setAppUser(user);

        Mark savedMark = markRepo.save(mark);

        User author = savedMark.getAppUser();
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
            User author = mark.getAppUser();
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

        User author = savedMark.getAppUser();
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

    public CommentDto addComment(Long markId, Comment cmt) throws Exception {
        Mark targetMark = markRepo.findById(markId)
            .orElseThrow(() -> new Exception("Not Found"));

        if (cmt.getUser() == null || !userRepo.existsById(cmt.getUser().getId())) {
            throw new Exception("User Does Not Exist");
        }

        cmt.setMark(targetMark);
        targetMark.setCmtCount(targetMark.getCmtCount() + 1);    

        markRepo.save(targetMark);
        Comment savedComment = cmtRepo.save(cmt);

        User commenter = savedComment.getUser();
        UserDto safeCommenter = new UserDto(commenter.getId(), commenter.getUsername());

        Mark commentedMark = savedComment.getMark();

        User markAuthor = commentedMark.getAppUser();
        UserDto safeMarkAuthor = new UserDto(markAuthor.getId(), markAuthor.getUsername());

        MarkDto safeCommentedMark = new MarkDto(
            commentedMark.getId(),
            safeMarkAuthor,
            commentedMark.getMessage(),
            commentedMark.getLatitude(),
            commentedMark.getLongitude(),
            commentedMark.getView(),
            commentedMark.getLikeCount(),
            commentedMark.getCmtCount(),
            commentedMark.isPrivacy()
        );

        return new CommentDto(
            savedComment.getId(),
            safeCommenter,
            safeCommentedMark,
            savedComment.getContent()
        );
    }
    
    public List<CommentDto> getAllComment(Long markId) {
        List<Comment> rawComments = cmtRepo.findByMarkId(markId);

        List<CommentDto> safeComments = new ArrayList<>();

        for (Comment comment : rawComments) {
            User commenter = comment.getUser();
            UserDto safeCommenter = new UserDto(commenter.getId(), commenter.getUsername());

            Mark commentedMark = comment.getMark();

            User markAuthor = commentedMark.getAppUser();
            UserDto safeMarkAuthor = new UserDto(markAuthor.getId(), markAuthor.getUsername());

            MarkDto safeCommentedMark = new MarkDto(
                commentedMark.getId(),
                safeMarkAuthor,
                commentedMark.getMessage(),
                commentedMark.getLatitude(),
                commentedMark.getLongitude(),
                commentedMark.getView(),
                commentedMark.getLikeCount(),
                commentedMark.getCmtCount(),
                commentedMark.isPrivacy()
            );

            CommentDto dto = new CommentDto(
                comment.getId(),
                safeCommenter,
                safeCommentedMark,
                comment.getContent()
            );

            safeComments.add(dto);
        }

        return safeComments;
    }

    public String likeMark(Long markId, Long userId) throws Exception {
        Mark mark = markRepo.findById(markId)
            .orElseThrow(() -> new Exception("Mark Not Found"));

        User user = userRepo.findById(userId)
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

    //was getAllMarks
    //refactor from using id to username
    //input the current user username using the app
    // and username of the person they want to see the marks
    public List<MarkDto> getMarks(String currentUser, String targetUser) {
        List<Mark> rawMarks;

        //this check if the target is the current user and fetch all of their marks for them
        if (targetUser.equals(currentUser)) {
            rawMarks = markRepo.findAllByAppUserUsername(currentUser);
        } else {
            boolean isAcceptedFriend = friendshipRepo.isAcceptedFriend(currentUser, targetUser);

            //check if target and current user are friend with each other
            if (isAcceptedFriend) {
                rawMarks = markRepo.findAllByAppUserUsername(targetUser);
            } else {
                //not friend (pending, rejected)
                //fetch only public marks 
                rawMarks = markRepo.findAllByAppUserUsernameAndPrivacyFalse(targetUser);
            }
        }

        List<MarkDto> safeMarks = new ArrayList<>();

        for (Mark mark : rawMarks) {
            
            User author = mark.getAppUser();
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
