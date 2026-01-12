package com.example.springbootdemo.service;

import com.example.springbootdemo.entity.MessageRecord;
import com.example.springbootdemo.mapper.ConversationMapper;
import com.example.springbootdemo.mapper.MessageRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.springbootdemo.entity.Conversation;
import com.example.springbootdemo.service.ConversationService;

/**
 * AI 聊天服务（保存对话记录并使用历史消息构造上下文）
 */
@Service
@Slf4j
public class AiChatService {

    @Resource
    private ChatModel qwenChatModel;

    @Resource
    private MessageRecordMapper messageRecordMapper;

    @Resource
    private ConversationMapper conversationMapper;

    @Resource
    private ConversationService conversationService;

    private static final String SYSTEM_MESSAGE = """
            你是一个智能助手，名叫"小智"。
            你的任务是帮助用户解答问题、提供建议和进行友好的对话。
            请用简洁、专业、友好的方式回答用户的问题。
            如果用户询问的内容你不确定，请诚实地告诉他们。
            回答请使用中文。
            """;

    /**
     * 聊天并保存上下文到数据库
    * @param conversationId 会话 ID（可为 null，表示不关联会话）
    * @param userMessage 用户消息
    * @param userId 可选：当前用户 ID（用于将会话绑定到用户）
    */
    public String chat(Long conversationId, String userMessage, Long userId) {
        // 如果没有传入会话，或会话不存在，则自动创建会话，标题为首次用户消息（或默认值）
        if (conversationId == null) {
            var res = conversationService.createConversation(userMessage == null ? null : (userMessage.length() > 100 ? userMessage.substring(0, 100) : userMessage), userId);
            if (res != null && res.getData() instanceof Conversation) {
                conversationId = ((Conversation) res.getData()).getId();
            }
        } else {
            try {
                var conv = conversationMapper.selectById(conversationId);
                if (conv == null) {
                    var res = conversationService.createConversation(userMessage == null ? null : (userMessage.length() > 100 ? userMessage.substring(0, 100) : userMessage), userId);
                    if (res != null && res.getData() instanceof Conversation) {
                        conversationId = ((Conversation) res.getData()).getId();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // 保存用户消息到数据库（支持较长文本）
        MessageRecord userRecord = new MessageRecord();
        userRecord.setConversationId(conversationId);
        userRecord.setRole("user");
        userRecord.setContent(userMessage);
        userRecord.setCreatedAt(LocalDateTime.now());
        messageRecordMapper.insert(userRecord);

        // 从数据库加载历史消息，按时间排序，构建上下文
        QueryWrapper<MessageRecord> qw = new QueryWrapper<>();
        qw.eq("conversation_id", conversationId).orderByAsc("created_at");
        List<MessageRecord> history = messageRecordMapper.selectList(qw);        // 构建 langchain4j 的消息列表：先 system，然后历史消息
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);

        // 将历史记录转换为相应的 ChatMessage 对象（UserMessage / AiMessage）
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(systemMessage);
        if (history != null) {
            for (MessageRecord m : history) {
                if ("user".equalsIgnoreCase(m.getRole())) {
                    messages.add(UserMessage.from(m.getContent()));
                } else {
                    messages.add(AiMessage.from(m.getContent()));
                }
            }
        }

        // 调用模型 - 使用 List<ChatMessage> 作为输入（langchain4j 支持）
        ChatResponse chatResponse;
        try {
            chatResponse = qwenChatModel.chat(messages);
        } catch (NoSuchMethodError | UnsupportedOperationException ex) {
            // 兼容性降级：尝试使用数组形式
            try {
                Object[] messageArray = messages.toArray();
                chatResponse = (ChatResponse) qwenChatModel.getClass().getMethod("chat", Object[].class).invoke(qwenChatModel, (Object) messageArray);
            } catch (Exception e) {
                log.error("调用模型失败", e);
                return "";
            }
        }
        if (chatResponse == null) {
            log.warn("未能获取模型响应，返回空字符串");
            return "";
        }
        AiMessage aiMessage = chatResponse.aiMessage();
        if (aiMessage == null) {
            log.warn("模型返回无 AI 消息，返回空字符串");
            return "";
        }
        log.info("AI 输出：{}", aiMessage.text());

        // 保存 AI 回复
        MessageRecord aiRecord = new MessageRecord();
        aiRecord.setConversationId(conversationId);
        aiRecord.setRole("ai");
        aiRecord.setContent(aiMessage.text());
        aiRecord.setCreatedAt(LocalDateTime.now());
        messageRecordMapper.insert(aiRecord);

        // 更新会话更新时间
        if (conversationId != null) {
            try {
                var conv = conversationMapper.selectById(conversationId);
                if (conv != null) {
                    conv.setUpdatedAt(LocalDateTime.now());
                    conversationMapper.updateById(conv);
                }
            } catch (Exception ignored) {
            }
        }

        return aiMessage.text();
    }

    /**
     * 聊天并返回conversationId和AI回复
     * @param conversationId 会话ID（可为null）
     * @param userMessage 用户消息
     * @param userId 用户ID
     * @return Map包含conversationId和response
     */
    public Map<String, Object> chatWithConversation(Long conversationId, String userMessage, Long userId) {
        String response = chat(conversationId, userMessage, userId);
        
        // 如果conversationId为null，需要查找刚创建的会话
        if (conversationId == null) {
            // 通过最新的消息记录找到对应的conversationId
            QueryWrapper<MessageRecord> qw = new QueryWrapper<>();
            qw.eq("content", userMessage).eq("role", "user").orderByDesc("created_at").last("LIMIT 1");
            MessageRecord lastMsg = messageRecordMapper.selectOne(qw);
            if (lastMsg != null) {
                conversationId = lastMsg.getConversationId();
            }
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("conversationId", conversationId);
        result.put("response", response);
        return result;
    }
}
