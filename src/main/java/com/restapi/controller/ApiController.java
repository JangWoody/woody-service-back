package com.restapi.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    private final TutoringService service;

    @GetMapping("/students")
    public List<StudentEntity> getStudents() {
        log.info("get students called");
        return service.getAllStudents();
    }

    @PostMapping("/students")
    public StudentEntity addStudent(@RequestBody Map<String, String> body) {
        log.info("post students called: {}", body.get("name"));
        return service.addStudent(body.get("name"));
    }

    @DeleteMapping("/students/{id}")
    public void deleteStudent(@PathVariable Long id) {
        log.info("delete students called: {}", id);
        service.deleteStudent(id);
    }

    @GetMapping("/schedules")
    public List<ScheduleEntity> getSchedules() {
        log.info("get schedules called");
        return service.getAllSchedules();
    }

    @PostMapping("/schedules")
    public ScheduleEntity createSchedule(@RequestBody ScheduleEntity schedule) {
        log.info("post schedules called: {}", schedule);
        return service.addSchedule(schedule);
    }

    @DeleteMapping("/schedules/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        log.info("delete schedules called: {}", id);
        service.deleteSchedule(id);
    }

    @PostMapping("/schedules/{id}/confirm")
    public void confirmSchedule(@PathVariable Long id) {
        log.info("confirm schedules called: {}", id);
        service.confirmSchedule(id);
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(@RequestBody Map<String, String> body) {
        log.info("login called");

        String password = body.get("secretKey");
        if (password == null || password.isBlank()) {
            password = body.get("password");
        }
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(false);
        }

        log.info("password received: {}", password.replaceAll(".", "*"));
        boolean ok = service.checkPassword(password);
        return ok ? ResponseEntity.ok(true) : ResponseEntity.status(401).body(false);
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody Map<String, String> body) {
        log.info("change password called");

        String newPassword = body.get("secretKey");
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = body.get("newPassword");
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        service.changePassword(newPassword);
        return ResponseEntity.ok().build();
    }
}
