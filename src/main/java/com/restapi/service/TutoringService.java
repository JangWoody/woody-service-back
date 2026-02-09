package com.restapi.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.restapi.entity.ScheduleEntity;
import com.restapi.entity.StudentEntity;
import com.restapi.entity.TeacherEntity;
import com.restapi.repository.ScheduleRepository;
import com.restapi.repository.StudentRepository;
import com.restapi.repository.TeacherRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class TutoringService {

    private final ScheduleRepository scheduleRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(JangWoodyService.class);

    private void validateFutureTime(LocalDate date, String timeStr) {
        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
        LocalDateTime scheduleDateTime = LocalDateTime.of(date, time);
        if (scheduleDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("과거의 스케줄은 신청, 수정, 삭제할 수 없습니다.");
        }
    }

    public void initTeacher() {
        if (teacherRepository.findByUsername("teacher").isEmpty()) {
            teacherRepository.save(TeacherEntity.builder()
                    .username("teacher")
                    .password(passwordEncoder.encode("1234"))
                    .build());
        }
    }

    public List<StudentEntity> getAllStudents() {
        return studentRepository.findAll();
    }

    public StudentEntity addStudent(String name) {
        if (studentRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 등록된 학생입니다.");
        }
        return studentRepository.save(StudentEntity.builder().name(name).build());
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public List<ScheduleEntity> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public ScheduleEntity addSchedule(ScheduleEntity schedule) {
        if (!studentRepository.existsByName(schedule.getStudentName())) {
            throw new IllegalArgumentException("등록되지 않은 학생입니다. 선생님께 문의하세요.");
        }

        validateFutureTime(schedule.getScheduleDate(), schedule.getScheduleTime());
        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        ScheduleEntity target = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스케줄이 없습니다."));
        validateFutureTime(target.getScheduleDate(), target.getScheduleTime());
        scheduleRepository.deleteById(id);
    }

    public void confirmSchedule(Long id) {
        ScheduleEntity target = scheduleRepository.findById(id).orElseThrow();
        validateFutureTime(target.getScheduleDate(), target.getScheduleTime());

        target.setStatus("confirmed");
        
        LocalDate targetDate = target.getScheduleDate();
        LocalDate startOfWeek = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<ScheduleEntity> studentSchedules = scheduleRepository.findByStudentName(target.getStudentName());
        for (ScheduleEntity other : studentSchedules) {
            boolean isSameWeek = !other.getScheduleDate().isBefore(startOfWeek) 
                              && !other.getScheduleDate().isAfter(endOfWeek);

            if (!other.getId().equals(id) 
                && "pending".equals(other.getStatus())
                && isSameWeek) {
                scheduleRepository.delete(other);
            }
        }
        
        List<ScheduleEntity> timeConflicts = scheduleRepository.findByScheduleDateAndScheduleTime(target.getScheduleDate(), target.getScheduleTime());
        for (ScheduleEntity other : timeConflicts) {
            if (!other.getId().equals(id) && "pending".equals(other.getStatus())) {
                scheduleRepository.delete(other);
            }
        }
    }

    public boolean checkPassword(String rawPassword) {
        log.info("login check: {}", "processing");
        initTeacher();
        TeacherEntity teacher = teacherRepository.findByUsername("teacher").orElseThrow();
        return passwordEncoder.matches(rawPassword, teacher.getPassword());
    }

    public void changePassword(String newPassword) {
        TeacherEntity teacher = teacherRepository.findByUsername("teacher").orElseThrow();
        teacher.setPassword(passwordEncoder.encode(newPassword));
        teacherRepository.save(teacher);
    }
}