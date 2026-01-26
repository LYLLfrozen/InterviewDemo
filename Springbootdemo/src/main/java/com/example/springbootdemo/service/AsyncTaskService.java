package com.example.springbootdemo.service;

import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;

/**
 * 异步任务服务
 * 处理非关键路径的操作，避免阻塞主请求
 */
@Service
public class AsyncTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);

    @Resource
    private CacheManager cacheManager;

    /**
     * 异步清除缓存
     * 用于在数据更新后异步清理相关缓存
     */
    @Async("taskExecutor")
    public void evictCacheAsync(String cacheName, Object key) {
        try {
            if (cacheManager.getCache(cacheName) != null) {
                cacheManager.getCache(cacheName).evict(key);
                logger.debug("异步清除缓存: {} - {}", cacheName, key);
            }
        } catch (Exception e) {
            logger.warn("异步清除缓存失败: {} - {}, 错误: {}", cacheName, key, e.getMessage());
        }
    }

    /**
     * 异步记录操作日志
     * 避免日志记录影响主业务性能
     */
    @Async("taskExecutor")
    public void logOperationAsync(String operation, Long userId, String details) {
        try {
            logger.info("用户操作: userId={}, operation={}, details={}", userId, operation, details);
            // 可扩展：写入操作日志表或发送到日志收集系统
        } catch (Exception e) {
            logger.warn("异步记录日志失败: {}", e.getMessage());
        }
    }

    /**
     * 异步发送消息通知（如有消息队列可替换为 MQ）
     */
    @Async("messageExecutor")
    public void sendNotificationAsync(Long userId, String message) {
        try {
            logger.info("发送通知: userId={}, message={}", userId, message);
            // 可扩展：通过 WebSocket、推送服务等发送实时通知
        } catch (Exception e) {
            logger.warn("异步发送通知失败: userId={}, 错误: {}", userId, e.getMessage());
        }
    }

    /**
     * 异步预热缓存
     * 在系统启动或定时任务中调用，提前加载热点数据
     */
    @Async("taskExecutor")
    public void warmUpCacheAsync(String cacheName) {
        try {
            logger.info("开始预热缓存: {}", cacheName);
            // 可根据具体业务实现缓存预热逻辑
            // 例如：加载热门用户、活跃好友列表等
        } catch (Exception e) {
            logger.warn("缓存预热失败: {}, 错误: {}", cacheName, e.getMessage());
        }
    }
}
