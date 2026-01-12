package com.example.springbootdemo.config;

import com.example.springbootdemo.service.ApiKeyService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;

/**
 * 动态 Qwen Chat Model
 * 在每次调用时从 Redis 获取最新的 API Key
 */
public class DynamicQwenChatModel implements ChatModel {

    private final ApiKeyService apiKeyService;

    public DynamicQwenChatModel(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /**
     * 获取实际的 ChatModel 实例
     */
    private QwenChatModel getActualModel() {
        String apiKey = apiKeyService.getDashscopeApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("API Key 未配置，请先在前端设置 DashScope API Key");
        }
        
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen-max")
                .build();
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        return getActualModel().chat(chatRequest);
    }

    @Override
    public ChatResponse chat(List<ChatMessage> list) {
        return getActualModel().chat(list);
    }
}
