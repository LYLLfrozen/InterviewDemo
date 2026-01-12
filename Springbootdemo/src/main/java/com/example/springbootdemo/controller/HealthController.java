package com.example.springbootdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    // 注意：项目配置了 context-path = /api，所以访问路径为: /api/health
    @GetMapping("/health")
    public String health() {
        return "ok";
    }
}
