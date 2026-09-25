package com.demo.my_backend_java.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "marks")

public class Mark {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User appUser;//foreign key 
    
    private String message;

    @Transient
    private Double latitude;

    @Transient
    private Double longitude;

    //postgis
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location; // Saves to PostGIS database

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "like_count")
    private int likeCount = 0;

    @Column(name = "cmt_count")
    private int cmtCount = 0;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private Instant createAt;

    private boolean privacy; 

    @Column(name = "photo_object_key")
    private String photoObjectKey;

    @Column(name = "photo_content_type")
    private String photoContentType;

    @Column(name = "photo_size_bytes")
    private Long photoSizeBytes;

    @Column(name = "photo_width")
    private Integer photoWidth;

    @Column(name = "photo_height")
    private Integer photoHeight;

    public Long getId() {
        return id;
    }

    public User getAppUser() {
        return appUser;
    }

    public void setAppUser(User appUser) {
        this.appUser = appUser;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public int getView() {
        return viewCount;
    }

    public void setView(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCmtCount() {
        return cmtCount;
    }

    public void setCmtCount(int cmtCount) {
        this.cmtCount = cmtCount;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    @PostLoad
    private void populateLatLngAfterLoad() {
        if (this.location != null) {
            this.latitude = this.location.getY();
            this.longitude = this.location.getX();
        }
    }

    public Instant getCreatedAt() {
        return createAt;
    }

    public void setCreatedAt(Instant createAt) {
        this.createAt = createAt;
    }

    public String getPhotoObjectKey() { 
        return photoObjectKey; 
    }

    public void setPhotoObjectKey(String photoObjectKey) { 
        this.photoObjectKey = photoObjectKey; 
    }

    public String getPhotoContentType() { 
        return photoContentType; 
    }

    public void setPhotoContentType(String photoContentType) { 
        this.photoContentType = photoContentType; 
    }

    public Long getPhotoSizeBytes() { 
        return photoSizeBytes; 
    }

    public void setPhotoSizeBytes(Long photoSizeBytes) { 
        this.photoSizeBytes = photoSizeBytes; 
    }

    public Integer getPhotoWidth() { 
        return photoWidth; 
    }

    public void setPhotoWidth(Integer photoWidth) { 
        this.photoWidth = photoWidth; 
    }

    public Integer getPhotoHeight() { 
        return photoHeight; 
    }
    
    public void setPhotoHeight(Integer photoHeight) { 
        this.photoHeight = photoHeight; 
    }

    public boolean hasPhoto() {
        return this.photoObjectKey != null && !this.photoObjectKey.isBlank();
    }
}
