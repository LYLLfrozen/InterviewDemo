package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 聊天控制器
 */
@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * AI 聊天接口
     * @param requestBody 包含用户消息的请求体 {"message": "用户消息", "conversationId": 123}
     * @return AI 的回复
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestHeader(value = "Authorization", required = false) String authorization,
                               @RequestHeader(value = "token", required = false) String tokenHeader,
                               @RequestBody Map<String, Object> requestBody) {
        try {
            Object msgObj = requestBody.get("message");
            if (msgObj == null || msgObj.toString().trim().isEmpty()) {
                return Result.error("消息不能为空");
            }
            String userMessage = msgObj.toString();

            Long conversationId = null;
            Object cid = requestBody.get("conversationId");
            if (cid != null) {
                try {
                    if (cid instanceof Number) conversationId = ((Number) cid).longValue();
                    else conversationId = Long.parseLong(cid.toString());
                } catch (Exception ignored) {
                }
            }

            // 从 token/Authorization 解析当前用户 ID（若有）
            Long userId = null;
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

            String aiResponse = aiChatService.chat(conversationId, userMessage, userId);
            return Result.success(aiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("AI 服务暂时不可用，请稍后再试: " + e.getMessage());
        }
    }
}
