package com.example.springbootdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message_record")
public class MessageRecord {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    /** 角色：user 或 ai */
    private String role;

    /** 支持较长文本，用于保存用户消息或 AI 回复 */
    private String content;

    private LocalDateTime createdAt;
}
