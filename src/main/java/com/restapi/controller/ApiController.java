package com.restapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // 추가
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restapi.entity.ScheduleEntity;
import com.restapi.entity.StudentEntity;
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String password = body.get("secretKey");
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "ok", false,
                "message", "password(pw)가 필요합니다."
            ));
        }

        boolean ok = service.checkPassword(password);

        if (ok) {
            return ResponseEntity.ok(Map.of("ok", true));
        } else {
            return ResponseEntity.status(401).body(Map.of("ok", false));
        }
   }

    @PostMapping("/password")
    public void changePassword(@RequestBody Map<String, String> body) { service.changePassword(body.get("secretKey")); }
}