package com.restapi.repository;

import com.restapi.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    List<ScheduleEntity> findByScheduleDateAndScheduleTime(LocalDate scheduleDate, String scheduleTime);
    List<ScheduleEntity> findByStudentName(String studentName);
}