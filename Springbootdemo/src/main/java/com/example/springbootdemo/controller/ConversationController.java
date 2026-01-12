package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.entity.Conversation;
import com.example.springbootdemo.entity.MessageRecord;
import com.example.springbootdemo.mapper.ConversationMapper;
import com.example.springbootdemo.mapper.MessageRecordMapper;
import com.example.springbootdemo.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageRecordMapper messageRecordMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("/create")
    public Result<?> create(@RequestHeader(value = "Authorization", required = false) String authorization,
                            @RequestHeader(value = "token", required = false) String tokenHeader,
                            @RequestBody Map<String, Object> body) {
        String title = body.getOrDefault("title", "").toString();
        Long userId = null;
        Object u = body.get("userId");
        if (u instanceof Number) userId = ((Number) u).longValue();

        // 若请求头带 token，则以 token 对应的 userId 为准
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        } else if (tokenHeader != null && !tokenHeader.isEmpty()) {
            token = tokenHeader;
        }
        if (token != null) {
            try {
                Object o = stringRedisTemplate.opsForValue().get("login:token:" + token);
                if (o != null) userId = Long.parseLong(o.toString());
            } catch (Exception ignored) {
            }
        }

        return conversationService.createConversation(title, userId);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable("id") Long id) {
        return conversationService.deleteConversation(id);
    }

    @GetMapping("/list")
    public Result<List<Conversation>> list(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @RequestHeader(value = "token", required = false) String tokenHeader) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        } else if (tokenHeader != null && !tokenHeader.isEmpty()) {
            token = tokenHeader;
        }

        Long userId = null;
        if (token != null) {
            try {
                Object o = stringRedisTemplate.opsForValue().get("login:token:" + token);
                if (o != null) userId = Long.parseLong(o.toString());
            } catch (Exception ignored) {
            }
        }

        // 如果有userId，返回该用户的会话；否则返回所有会话（方便测试）
        List<Conversation> list;
        if (userId != null) {
            list = conversationMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Conversation>()
                            .eq("user_id", userId)
                            .orderByDesc("updated_at")
            );
        } else {
            // 没有userId时返回所有会话（包括user_id为null的）
            list = conversationMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Conversation>()
                            .orderByDesc("updated_at")
            );
        }
        return Result.success(list);
    }

    @GetMapping("/{id}/messages")
    public Result<List<MessageRecord>> messages(@PathVariable("id") Long id,
                                                @RequestHeader(value = "Authorization", required = false) String authorization,
                                                @RequestHeader(value = "token", required = false) String tokenHeader) {
        Conversation conv = conversationMapper.selectById(id);
        if (conv == null) return Result.error("会话不存在");

        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        } else if (tokenHeader != null && !tokenHeader.isEmpty()) {
            token = tokenHeader;
        }

        Long userId = null;
        if (token != null) {
            try {
                Object o = stringRedisTemplate.opsForValue().get("login:token:" + token);
                if (o != null) userId = Long.parseLong(o.toString());
            } catch (Exception ignored) {
            }
        }

        if (conv.getUserId() != null) {
            if (userId == null || !conv.getUserId().equals(userId)) {
                return Result.error("无权查看该会话消息");
            }
        }

        List<MessageRecord> msgs = messageRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<MessageRecord>()
                        .eq("conversation_id", id)
                        .orderByAsc("created_at")
        );
        return Result.success(msgs);
    }
}
