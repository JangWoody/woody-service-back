package com.restapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // 중요: "/reservation" 뒤에 점(.)이 들어간 파일(html, js, css 등)은 무시하고
    // 순수 페이지 경로(예: /reservation, /reservation/login)만 잡아서 index.html로 보냅니다.
    @GetMapping({"/reservation", "/reservation/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/reservation/index.html";
    }
}