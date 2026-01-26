package com.example.springbootdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootdemo.entity.Friend;
import com.example.springbootdemo.entity.FriendRequest;
import com.example.springbootdemo.entity.Message;
import com.example.springbootdemo.mapper.FriendMapper;
import com.example.springbootdemo.mapper.FriendRequestMapper;
import com.example.springbootdemo.mapper.MessageMapper;
import com.example.springbootdemo.service.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社交功能服务实现类
 */
@Service
public class SocialServiceImpl implements SocialService {

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private com.example.springbootdemo.mapper.UserMapper userMapper;    @Override
    @Transactional
    public void sendFriendRequestByUsername(Long fromUserId, String toUsername) {
        if (toUsername == null || toUsername.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        // 根据用户名查找目标用户
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.springbootdemo.entity.User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.example.springbootdemo.entity.User::getUsername, toUsername);
        com.example.springbootdemo.entity.User toUser = userMapper.selectOne(wrapper);
        
        if (toUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        sendFriendRequest(fromUserId, toUser.getId());
    }
    
    @Override
    @Transactional
    public void sendFriendRequest(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("不能向自己发送好友请求");
        }

        // 检查是否已经是好友
        if (isFriend(fromUserId, toUserId)) {
            throw new RuntimeException("已经是好友关系");
        }

        // 检查是否已经发送过待处理的请求
        QueryWrapper<FriendRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_user_id", fromUserId)
                   .eq("to_user_id", toUserId)
                   .eq("status", 0);
        FriendRequest existingRequest = friendRequestMapper.selectOne(queryWrapper);
        if (existingRequest != null) {
            throw new RuntimeException("已经发送过好友请求，请等待对方处理");
        }

        // 创建好友请求
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setFromUserId(fromUserId);
        friendRequest.setToUserId(toUserId);
        friendRequest.setStatus(0);
        friendRequest.setCreatedAt(LocalDateTime.now());
        friendRequest.setUpdatedAt(LocalDateTime.now());
        friendRequestMapper.insert(friendRequest);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "friendList", key = "#friendRequest.fromUserId"),
        @CacheEvict(value = "friendList", key = "#friendRequest.toUserId"),
        @CacheEvict(value = "friendRequest", key = "#userId")
    })
    public void acceptFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestMapper.selectById(requestId);
        if (friendRequest == null) {
            throw new RuntimeException("好友请求不存在");
        }
        if (!friendRequest.getToUserId().equals(userId)) {
            throw new RuntimeException("无权处理此请求");
        }
        if (friendRequest.getStatus() != 0) {
            throw new RuntimeException("请求已被处理");
        }

        // 更新请求状态
        friendRequest.setStatus(1);
        friendRequest.setUpdatedAt(LocalDateTime.now());
        friendRequestMapper.updateById(friendRequest);

        // 创建双向好友关系
        Friend friend1 = new Friend();
        friend1.setUserId(friendRequest.getFromUserId());
        friend1.setFriendId(friendRequest.getToUserId());
        friend1.setCreatedAt(LocalDateTime.now());
        friendMapper.insert(friend1);

        Friend friend2 = new Friend();
        friend2.setUserId(friendRequest.getToUserId());
        friend2.setFriendId(friendRequest.getFromUserId());
        friend2.setCreatedAt(LocalDateTime.now());
        friendMapper.insert(friend2);
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long requestId, Long userId) {
        FriendRequest friendRequest = friendRequestMapper.selectById(requestId);
        if (friendRequest == null) {
            throw new RuntimeException("好友请求不存在");
        }
        if (!friendRequest.getToUserId().equals(userId)) {
            throw new RuntimeException("无权处理此请求");
        }
        if (friendRequest.getStatus() != 0) {
            throw new RuntimeException("请求已被处理");
        }

        // 更新请求状态
        friendRequest.setStatus(2);
        friendRequest.setUpdatedAt(LocalDateTime.now());
        friendRequestMapper.updateById(friendRequest);
    }

    @Override
    @Cacheable(value = "friendRequest", key = "#userId")
    public List<FriendRequest> getPendingFriendRequests(Long userId) {
        QueryWrapper<FriendRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_user_id", userId)
                   .eq("status", 0)
                   .orderByDesc("created_at");
        return friendRequestMapper.selectList(queryWrapper);
    }

    @Override
    public List<FriendRequest> getSentFriendRequests(Long userId) {
        QueryWrapper<FriendRequest> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("from_user_id", userId)
                   .orderByDesc("created_at");
        return friendRequestMapper.selectList(queryWrapper);
    }

    @Override
    @Cacheable(value = "friendList", key = "#userId")
    public List<Friend> getFriendList(Long userId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("created_at");
        return friendMapper.selectList(queryWrapper);
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        QueryWrapper<Friend> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("friend_id", friendId);
        long count = friendMapper.selectCount(queryWrapper);
        System.out.println("检查好友关系: userId=" + userId + ", friendId=" + friendId + ", count=" + count);
        return count > 0;
    }    @Override
    @Transactional
    public Message sendMessageByUsername(Long fromUserId, String toUsername, String content) {
        if (toUsername == null || toUsername.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        
        // 根据用户名查找目标用户
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.springbootdemo.entity.User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.example.springbootdemo.entity.User::getUsername, toUsername);
        com.example.springbootdemo.entity.User toUser = userMapper.selectOne(wrapper);
        
        if (toUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        return sendMessage(fromUserId, toUser.getId(), content);
    }
    
    @Override
    @Transactional
    public Message sendMessageByUsername(Long fromUserId, String toUsername, String content, boolean persist) {
        if (toUsername == null || toUsername.trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        // 根据用户名查找目标用户
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.example.springbootdemo.entity.User> wrapper = 
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.example.springbootdemo.entity.User::getUsername, toUsername);
        com.example.springbootdemo.entity.User toUser = userMapper.selectOne(wrapper);
        if (toUser == null) {
            throw new RuntimeException("用户不存在");
        }
        return sendMessage(fromUserId, toUser.getId(), content, persist);
    }
    
    @Override
    @Transactional
    // 移除缓存清理注解，因为已经不使用聊天消息缓存了
    @CacheEvict(value = "unreadCount", key = "#toUserId")
    public Message sendMessage(Long fromUserId, Long toUserId, String content) {
        System.out.println("=== 发送消息 ===");
        System.out.println("发送者ID: " + fromUserId);
        System.out.println("接收者ID: " + toUserId);
        System.out.println("消息内容: " + content);
        
        // 检查是否为好友关系
        boolean areFriends = isFriend(fromUserId, toUserId);
        System.out.println("是否为好友关系: " + areFriends);
        
        if (!areFriends) {
            System.err.println("❌ 错误：用户 " + fromUserId + " 和 " + toUserId + " 不是好友关系");
            throw new RuntimeException("只能向好友发送消息");
        }

        Message message = new Message();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setIsRead(0);
        message.setTimestamp(LocalDateTime.now());
        messageMapper.insert(message);
        
        System.out.println("✅ 消息发送成功，消息ID: " + message.getId());
        return message;
    }

    @Override
    public Message sendMessage(Long fromUserId, Long toUserId, String content, boolean persist) {
        if (!persist) {
            // 压测模式：不持久化，仅返回构造的消息对象
            Message message = new Message();
            message.setFromUserId(fromUserId);
            message.setToUserId(toUserId);
            message.setContent(content);
            message.setIsRead(0);
            message.setTimestamp(LocalDateTime.now());
            return message;
        }
        // persist == true：调用原有持久化实现（包含缓存清理注解）
        return sendMessage(fromUserId, toUserId, content);
    }

    @Override
    // 移除缓存，确保总是获取最新消息
    // @Cacheable(value = "chatMessages", key = "#userId + ':' + #friendId + ':' + #limit")
    public List<Message> getChatMessages(Long userId, Long friendId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50;
        }

        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .and(w -> w.eq("from_user_id", userId).eq("to_user_id", friendId))
                .or(w -> w.eq("from_user_id", friendId).eq("to_user_id", userId))
        ).orderByDesc("timestamp")
         .last("LIMIT " + limit);
        
        List<Message> messages = messageMapper.selectList(queryWrapper);
        // 反转列表，使最新的消息在最后
        java.util.Collections.reverse(messages);
        return messages;
    }

    @Override
    @Cacheable(value = "unreadCount", key = "#userId")
    public int getUnreadCount(Long userId) {
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("to_user_id", userId)
                   .eq("is_read", 0);
        return messageMapper.selectCount(queryWrapper).intValue();
    }

    @Override
    @Transactional
    @CacheEvict(value = "unreadCount", key = "#userId")
    public void markAsRead(Long messageId, Long userId) {
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        if (!message.getToUserId().equals(userId)) {
            throw new RuntimeException("无权操作此消息");
        }
        message.setIsRead(1);
        messageMapper.updateById(message);
    }
}
