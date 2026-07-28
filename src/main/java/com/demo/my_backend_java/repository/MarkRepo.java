package com.demo.my_backend_java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.demo.my_backend_java.entity.Mark;

public interface MarkRepo extends JpaRepository<Mark, Long>{

    boolean existsByLongitude(double longitude);

    boolean existsByLatitude(double latitude);

    List<Mark> findAllByAppUserId(Long id);

    List<Mark> findAllByAppUserUsername(String username);

    List<Mark> findAllByAppUserUsernameAndPrivacyFalse(String username);

    // Calculates Earth's radius (6371000m) to find marks within 50 meters
    // @Query(value = "SELECT * FROM marks WHERE (6371000 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) <= 50", nativeQuery = true)
    // List<Mark> findNearbyMarks(@Param("lat") Double lat, @Param("lng") Double lng);

    // ST_DWithin checks if two geometries are within a specified distance.
    // The 'true' at the end tells it to use spheroidal math (meters) instead of map degrees.
    @Query(value = "SELECT m.* FROM marks m " +
            "WHERE ST_DWithin(m.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), :radius, true) " +
            "AND ( " +
            "   m.privacy = false " + // Public mark
            "   OR m.user_id = :userId " + // User's own private mark
            "   OR EXISTS ( " + // Accepted friend's private mark
            "       SELECT 1 FROM friendships f " +
            "       WHERE f.status = 'ACCEPTED' " +
            "       AND ( " +
            "           (f.requested_id = m.user_id AND f.receiver_id = :userId) " +
            "           OR (f.requested_id = :userId AND f.receiver_id = m.user_id) " +
            "       ) " +
            "   ) " +
            ") " +
            "ORDER BY ST_Distance(m.location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)) " +
            "LIMIT :limit", nativeQuery = true)
    List<Mark> findNearbyMarks(
        @Param("lat") Double lat, 
        @Param("lng") Double lng, 
        @Param("userId") Long userId,
        @Param("radius") double radius,
        @Param("limit") int limit
    );

    @Query(value = "SELECT m.* FROM marks m " +
            "LEFT JOIN friendships f ON ( " +
            "   (f.requested_id = :userId AND f.receiver_id = m.user_id) " +
            "   OR (f.requested_id = m.user_id AND f.receiver_id = :userId) " +
            ") AND f.status = 'ACCEPTED' " +
            "WHERE ST_Intersects(m.location, ST_MakeEnvelope(:minLng, :minLat, :maxLng, :maxLat, 4326)) " +
            "AND (m.user_id = :userId OR f.id IS NOT NULL) " +
            "LIMIT 100", nativeQuery = true)
    List<Mark> findBoundedFriendsAndOwnMarks(
        @Param("minLng") double minLng, 
        @Param("minLat") double minLat, 
        @Param("maxLng") double maxLng, 
        @Param("maxLat") double maxLat, 
        @Param("userId") Long userId
    );
}
