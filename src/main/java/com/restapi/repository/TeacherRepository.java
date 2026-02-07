package com.restapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restapi.entity.TeacherEntity;

public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {
    Optional<TeacherEntity> findByUsername(String username);
}