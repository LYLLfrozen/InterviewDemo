package com.example.springbootdemo.service.impl;

import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.entity.Conversation;
import com.example.springbootdemo.mapper.ConversationMapper;
import com.example.springbootdemo.service.ConversationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class ConversationServiceImpl implements ConversationService {

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    public Result<?> createConversation(String title, Long userId) {
        Conversation c = new Conversation();
        c.setTitle(title == null || title.trim().isEmpty() ? "新会话" : title);
        c.setUserId(userId);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(c);
        return Result.success(c);
    }

    @Override
    public Result<?> deleteConversation(Long conversationId) {
        int deleted = conversationMapper.deleteById(conversationId);
        if (deleted > 0) return Result.success("删除成功");
        return Result.error("没有找到会话");
    }
}
