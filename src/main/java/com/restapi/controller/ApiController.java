package com.restapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.restapi.entity.ScheduleEntity;
import com.restapi.entity.StudentEntity; // 추가
import com.restapi.service.TutoringService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ApiController {

    private final TutoringService service;

    @GetMapping("/students")
    public List<StudentEntity> getStudents() {
        return service.getAllStudents();
    }

    @PostMapping("/students")
    public StudentEntity addStudent(@RequestBody Map<String, String> body) {
        return service.addStudent(body.get("name"));
    }

    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable Long id) {
        service.deleteStudent(id);
    }

    @GetMapping("/schedules")
    public List<ScheduleEntity> getSchedules() { return service.getAllSchedules(); }

    @PostMapping("/schedules")
    public ScheduleEntity createSchedule(@RequestBody ScheduleEntity schedule) { return service.addSchedule(schedule); }

    @DeleteMapping("/schedules/{id}")
    public void deleteSchedule(@PathVariable Long id) { service.deleteSchedule(id); }

    @PostMapping("/schedules/{id}/confirm")
    public void confirmSchedule(@PathVariable Long id) { service.confirmSchedule(id); }

    @PostMapping("/login")
    public boolean login(@RequestBody Map<String, String> body) { return service.checkPassword(body.get("password")); }

    @PostMapping("/password")
    public void changePassword(@RequestBody Map<String, String> body) { service.changePassword(body.get("newPassword")); }
}