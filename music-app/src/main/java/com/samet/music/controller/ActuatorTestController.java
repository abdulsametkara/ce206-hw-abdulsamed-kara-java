package com.samet.music.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/actuator/custom")
public class ActuatorTestController {
    
    @GetMapping
    public Map<String, Object> testActuator() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Custom actuator endpoint is working");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
} 