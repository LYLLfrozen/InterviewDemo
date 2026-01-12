package com.example.springbootdemo.service;

import com.example.springbootdemo.common.Result;

public interface ConversationService {
    Result<?> createConversation(String title, Long userId);
    Result<?> deleteConversation(Long conversationId);
}
