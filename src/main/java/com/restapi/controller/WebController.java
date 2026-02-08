package com.restapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({"/reservation", "/reservation/{path:[^\\.]*}", "/reservation/**/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/reservation/index.html";
    }
}
