package com.restapi.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restapi.entity.ScheduleEntity;
import com.restapi.entity.TeacherEntity;
import com.restapi.repository.ScheduleRepository;
import com.restapi.repository.TeacherRepository;

@Service
@Transactional
public class TutoringService {

    private final ScheduleRepository scheduleRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public TutoringService(ScheduleRepository scheduleRepository, TeacherRepository teacherRepository, PasswordEncoder passwordEncoder) {
        this.scheduleRepository = scheduleRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
        
        // 초기 선생님 계정 생성 (비번: 1234)
        if (teacherRepository.findByUsername("teacher").isEmpty()) {
            teacherRepository.save(TeacherEntity.builder()
                    .username("teacher")
                    .password(passwordEncoder.encode("1234"))
                    .build());
        }
    }

    public List<ScheduleEntity> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public ScheduleEntity addSchedule(ScheduleEntity schedule) {
        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }

    public void confirmSchedule(Long id) {
        ScheduleEntity target = scheduleRepository.findById(id).orElseThrow();
        target.setStatus("confirmed");
        
        List<ScheduleEntity> others = scheduleRepository.findByScheduleDateAndScheduleTime(target.getScheduleDate(), target.getScheduleTime());
        for (ScheduleEntity other : others) {
            if (!other.getId().equals(id) && "pending".equals(other.getStatus())) {
                scheduleRepository.delete(other);
            }
        }
    }

    public boolean checkPassword(String rawPassword) {
        TeacherEntity teacher = teacherRepository.findByUsername("teacher").orElseThrow();
        return passwordEncoder.matches(rawPassword, teacher.getPassword());
    }

    public void changePassword(String newPassword) {
        TeacherEntity teacher = teacherRepository.findByUsername("teacher").orElseThrow();
        teacher.setPassword(passwordEncoder.encode(newPassword));
        teacherRepository.save(teacher);
    }
}