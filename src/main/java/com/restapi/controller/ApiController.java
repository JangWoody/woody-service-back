package com.restapi.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.restapi.service.CredentialCryptoService;
import com.restapi.service.TutoringService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ApiController {

    private static final Logger log = LoggerFactory.getLogger(ApiController.class);
    private static final String TEACHER_AUTH_SESSION_KEY = "teacherAuth";
    private final TutoringService service;
    private final CredentialCryptoService credentialCryptoService;

    @GetMapping("/login/public-key")
    public ResponseEntity<Map<String, String>> getLoginPublicKey() {
        return ResponseEntity.ok(Map.of(
                "alg", "RSA-OAEP-256",
                "publicKeyPem", credentialCryptoService.getPublicKeyPem()));
    }

    @GetMapping("/students")
    public List<StudentEntity> getStudents() {
        log.info("get students called");
        return service.getAllStudents();
    }

    @PostMapping("/students")
    public ResponseEntity<StudentEntity> addStudent(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        if (!isTeacherAuthenticated(request)) {
            return ResponseEntity.status(403).build();
        }
        log.info("post students called: {}", body.get("name"));
        return ResponseEntity.ok(service.addStudent(body.get("name")));
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id, HttpServletRequest request) {
        if (!isTeacherAuthenticated(request)) {
            return ResponseEntity.status(403).build();
        }
        log.info("delete students called: {}", id);
        service.deleteStudent(id);
        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> confirmSchedule(@PathVariable Long id, HttpServletRequest request) {
        if (!isTeacherAuthenticated(request)) {
            return ResponseEntity.status(403).build();
        }
        log.info("confirm schedules called: {}", id);
        service.confirmSchedule(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        log.info("login called");

        String password = resolveSecret(
                body,
                List.of("secretKeyEncrypted", "passwordEncrypted"),
                List.of());
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(false);
        }

        boolean ok = service.checkPassword(password);
        if (ok) {
            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute(TEACHER_AUTH_SESSION_KEY, true);
            return ResponseEntity.ok(true);
        }
        clearTeacherSession(request);
        return ResponseEntity.status(401).body(false);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        clearTeacherSession(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/teacher/session")
    public ResponseEntity<Map<String, Boolean>> teacherSession(HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("ok", isTeacherAuthenticated(request)));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        if (!isTeacherAuthenticated(request)) {
            return ResponseEntity.status(403).build();
        }
        log.info("change password called");

        String newPassword = resolveSecret(
                body,
                List.of("newPasswordEncrypted", "secretKeyEncrypted"),
                List.of());
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        service.changePassword(newPassword);
        return ResponseEntity.ok().build();
    }

    private boolean isTeacherAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute(TEACHER_AUTH_SESSION_KEY));
    }

    private void clearTeacherSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private String resolveSecret(
            Map<String, String> body,
            List<String> encryptedKeys,
            List<String> plainKeys) {

        String encrypted = firstNonBlank(body, encryptedKeys);
        if (encrypted != null) {
            try {
                return credentialCryptoService.decryptBase64(encrypted);
            } catch (IllegalArgumentException e) {
                log.warn("Failed to decrypt credential payload.");
                return null;
            }
        }
        return firstNonBlank(body, plainKeys);
    }

    private String firstNonBlank(Map<String, String> body, List<String> keys) {
        return keys.stream()
                .map(body::get)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .findFirst()
                .orElse(null);
    }
}
