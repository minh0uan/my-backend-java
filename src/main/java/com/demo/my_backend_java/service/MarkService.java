package com.demo.my_backend_java.service;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        //post gis
        if (mark.getLatitude() != null && mark.getLongitude() != null) {
            GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

            mark.setLocation(geometryFactory.createPoint(new Coordinate(mark.getLongitude(), mark.getLatitude())));
        }
        
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

    public List<MarkDto> getNearbyMarks(Double lat, Double lng, String username){

        User requestingUser = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User Does Not Exist"));

        List<Mark> rawMarks = markRepo.findNearbyMarks(lat, lng, requestingUser.getId());

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

    //transaction because saving on 2 database at once
    //if one failed the other one will rollback too
    @Transactional
    public CommentDto addComment(Long markId, Comment cmt) throws Exception {
        Mark targetMark = markRepo.findById(markId)
            .orElseThrow(() -> new Exception("Not Found"));

        if (cmt.getUser() == null || !userRepo.existsById(cmt.getUser().getId())) {
            throw new Exception("User Does Not Exist");
        }

        User markOwner = targetMark.getAppUser();
        User rawCommenter = cmt.getUser();

        //check privacy and allow user to comment on their own mark (edge case)
        if (targetMark.isPrivacy() && !markOwner.getUsername().equals(rawCommenter.getUsername())) {
            if (!friendshipRepo.isAcceptedFriend(markOwner.getUsername(), rawCommenter.getUsername())) {
               throw new RuntimeException("Error: Cant Comment On A Private Mark");
            } 
        }

        cmt.setMark(targetMark);
        targetMark.setCmtCount(targetMark.getCmtCount() + 1);    

        markRepo.save(targetMark);
        Comment savedComment = cmtRepo.save(cmt);

        UserDto safeCommenter = new UserDto(rawCommenter.getId(), rawCommenter.getUsername());

        UserDto safeMarkAuthor = new UserDto(markOwner.getId(), markOwner.getUsername());

        Mark commentedMark = savedComment.getMark();

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
    
    public List<CommentDto> getComments(Long markId) {
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

    @Transactional
    public String likeMark(Long markId, Long userId){
        Mark mark = markRepo.findById(markId)
            .orElseThrow(() -> new RuntimeException("Mark Not Found"));

        User liker = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User Not Found"));

        User markOwner = mark.getAppUser();
        
        if (mark.isPrivacy() && !markOwner.getUsername().equals(liker.getUsername())) {
            if (!friendshipRepo.isAcceptedFriend(markOwner.getUsername(), liker.getUsername())) {
               throw new RuntimeException("Error: Cant Like A Private Mark");
            } 
        }

        if (likeRepo.existsByMarkIdAndUserId(markId, userId)) {
            throw new RuntimeException("User Already Like This Mark");
        }

        Like newLike = new Like();
        newLike.setMark(mark);
        newLike.setUser(liker);
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
            //check if target and current user are friend with each other
            if (friendshipRepo.isAcceptedFriend(currentUser, targetUser)) {
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

    public MarkDto getMarkById(Long markId){
        Mark mark = markRepo.findById(markId)
            .orElseThrow(() -> new RuntimeException("Error: Mark Not Found"));


        User author = mark.getAppUser();
        UserDto safeAuthor = new UserDto(author.getId(), author.getUsername());

        return new MarkDto(
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
