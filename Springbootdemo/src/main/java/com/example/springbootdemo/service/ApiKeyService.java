package com.example.springbootdemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * API Key 管理服务
 * 用于管理 AI 模型的 API Key，存储在 Redis 中
 */
@Service
public class ApiKeyService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String DASHSCOPE_API_KEY = "config:dashscope:api-key";

    /**
     * 设置 DashScope API Key
     * @param apiKey API Key
     */
    public void setDashscopeApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key 不能为空");
        }
        stringRedisTemplate.opsForValue().set(DASHSCOPE_API_KEY, apiKey.trim());
    }

    /**
     * 获取 DashScope API Key
     * @return API Key，如果未设置则返回 null
     */
    public String getDashscopeApiKey() {
        return stringRedisTemplate.opsForValue().get(DASHSCOPE_API_KEY);
    }

    /**
     * 删除 DashScope API Key
     */
    public void deleteDashscopeApiKey() {
        stringRedisTemplate.delete(DASHSCOPE_API_KEY);
    }

    /**
     * 检查是否已配置 API Key
     * @return true 如果已配置，false 如果未配置
     */
    public boolean hasApiKey() {
        String key = getDashscopeApiKey();
        return key != null && !key.trim().isEmpty();
    }
}
