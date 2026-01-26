package com.example.springbootdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查和性能监控接口
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 简单健康检查（保持原有接口）
     */
    @GetMapping
    public String health() {
        return "ok";
    }

    /**
     * 综合健康检查
     */
    @GetMapping("/check")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());

        // 检查数据库连接
        try (Connection conn = dataSource.getConnection()) {
            health.put("database", "UP");
            health.put("dbConnection", conn.isValid(3) ? "OK" : "SLOW");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("dbError", e.getMessage());
            health.put("status", "DEGRADED");
        }

        // 检查 Redis 连接
        try {
            redisTemplate.execute((RedisConnection connection) -> {
                connection.ping();
                return "OK";
            });
            health.put("redis", "UP");
        } catch (Exception e) {
            health.put("redis", "DOWN");
            health.put("redisError", e.getMessage());
            health.put("status", "DEGRADED");
        }

        return health;
    }

    /**
     * 系统性能指标
     */
    @GetMapping("/metrics")
    public Map<String, Object> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // JVM 内存信息
        Map<String, Object> memory = new HashMap<>();
        memory.put("maxMemoryMB", maxMemory / 1024 / 1024);
        memory.put("totalMemoryMB", totalMemory / 1024 / 1024);
        memory.put("usedMemoryMB", usedMemory / 1024 / 1024);
        memory.put("freeMemoryMB", freeMemory / 1024 / 1024);
        memory.put("usagePercent", String.format("%.2f%%", (usedMemory * 100.0) / maxMemory));
        metrics.put("memory", memory);

        // 线程信息
        Map<String, Object> threads = new HashMap<>();
        threads.put("activeCount", Thread.activeCount());
        threads.put("peakCount", ManagementFactory.getThreadMXBean().getPeakThreadCount());
        metrics.put("threads", threads);

        // CPU 信息
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("availableProcessors", runtime.availableProcessors());
        cpu.put("systemLoadAverage", ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        metrics.put("cpu", cpu);

        // 运行时间
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        metrics.put("uptimeSeconds", uptimeMillis / 1000);
        metrics.put("uptimeMinutes", uptimeMillis / 1000 / 60);

        return metrics;
    }

    /**
     * 简单的 Ping 接口（用于快速健康检查）
     */
    @GetMapping("/ping")
    public Map<String, String> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "pong");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }
}

