package com.proyecto.emilite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    // Endpoint: GET /
    @GetMapping("/")
    public String landing() {
        return "landing"; // Devuelve la vista landing.html
    }
}