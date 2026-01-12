package com.example.springbootdemo.config;

import com.example.springbootdemo.service.ApiKeyService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI Chat Model 配置
 * 创建动态配置的 ChatModel Bean
 */
@Configuration
public class AiChatConfig {

    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * 创建 QwenChatModel Bean
     * 使用懒加载，每次调用时从 Redis 获取最新的 API Key
     */
    @Bean
    public ChatModel qwenChatModel() {
        // 返回一个代理实现，每次调用时动态获取 API Key
        return new DynamicQwenChatModel(apiKeyService);
    }
}
