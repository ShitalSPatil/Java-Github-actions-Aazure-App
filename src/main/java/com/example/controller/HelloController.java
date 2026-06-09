package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for basic endpoints
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Azure App Service!";
    }

    @GetMapping("/status")
    public String status() {
        return "Application is running successfully on Azure App Service";
    }

    @GetMapping("/version")
    public String version() {
        return "Version: 1.0.0";
    }
}
