package com.restapi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restapi.entity.ScheduleEntity;
import com.restapi.service.TutoringService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ApiController {

    private final TutoringService service;

    // 스케줄 목록 조회
    @GetMapping("/schedules")
    public List<ScheduleEntity> getSchedules() {
        return service.getAllSchedules();
    }

    // 스케줄 신청
    @PostMapping("/schedules")
    public ScheduleEntity createSchedule(@RequestBody ScheduleEntity schedule) {
        return service.addSchedule(schedule);
    }

    // 스케줄 삭제
    @DeleteMapping("/schedules/{id}")
    public void deleteSchedule(@PathVariable Long id) {
        service.deleteSchedule(id);
    }

    // 스케줄 확정
    @PostMapping("/schedules/{id}/confirm")
    public void confirmSchedule(@PathVariable Long id) {
        service.confirmSchedule(id);
    }

    // 로그인 체크
    @PostMapping("/login")
    public boolean login(@RequestBody Map<String, String> body) {
        return service.checkPassword(body.get("password"));
    }

    // 비번 변경
    @PostMapping("/password")
    public void changePassword(@RequestBody Map<String, String> body) {
        service.changePassword(body.get("newPassword"));
    }
}