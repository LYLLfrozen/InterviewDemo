package com.example.springbootdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友请求实体类
 */
@Data
@TableName("friend_request")
public class FriendRequest {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发起请求的用户ID
     */
    private Long fromUserId;

    /**
     * 接收请求的用户ID
     */
    private Long toUserId;

    /**
     * 状态：0=待处理，1=已接受，2=已拒绝
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
