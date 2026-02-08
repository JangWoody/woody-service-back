package com.restapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({"/reservation", "/reservation/"})
    public String reservationEntry() {
        return "forward:/reservation/index.html";
    }
}
