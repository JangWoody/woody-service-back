package com.restapi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restapi.entity.ScheduleEntity;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByScheduleDateAndScheduleTime(LocalDate date, String time);
}