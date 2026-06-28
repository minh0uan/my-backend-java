package com.demo.my_backend_java.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.Mark;

@Repository
public interface MarkRepo extends JpaRepository<Mark, Long>{

    boolean existsByLongitude(double longitude);

    boolean existsByLatitude(double latitude);

    List<Mark> findAllByAppUserId(Long id);

    // Calculates Earth's radius (6371000m) to find marks within 50 meters
    @Query(value = "SELECT * FROM marks WHERE (6371000 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) <= 50", nativeQuery = true)
    List<Mark> findNearbyMarks(@Param("lat") Double lat, @Param("lng") Double lng);
}
