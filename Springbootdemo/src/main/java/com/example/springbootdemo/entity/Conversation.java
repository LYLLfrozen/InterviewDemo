package com.example.springbootdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 可选：关联用户 ID */
    private Long userId;

    /** 会话标题 */
    private String title;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
