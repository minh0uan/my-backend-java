package com.demo.my_backend_java.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.my_backend_java.entity.Mark;

@Repository
public interface MarkRepo extends JpaRepository<Mark, Long>{

    boolean existBylongitude(double longitude);

    boolean existByLatitude(double latitude);

    Optional<Mark> findByLongitude(double longitude);

    Optional<Mark> findByLatitude(double latitude);
}
