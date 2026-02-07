package com.restapi.repository;

import com.restapi.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    Optional<StudentEntity> findByName(String name);
    boolean existsByName(String name);
}