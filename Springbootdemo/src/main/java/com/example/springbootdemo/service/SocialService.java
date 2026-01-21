package com.example.springbootdemo.service;

import com.example.springbootdemo.entity.Friend;
import com.example.springbootdemo.entity.FriendRequest;
import com.example.springbootdemo.entity.Message;

import java.util.List;

/**
 * 社交功能服务接口
 */
public interface SocialService {
    /**
     * 发送好友请求（通过用户名）
     */
    void sendFriendRequestByUsername(Long fromUserId, String toUsername);
    
    /**
     * 发送好友请求
     */
    void sendFriendRequest(Long fromUserId, Long toUserId);

    /**
     * 接受好友请求
     */
    void acceptFriendRequest(Long requestId, Long userId);

    /**
     * 拒绝好友请求
     */
    void rejectFriendRequest(Long requestId, Long userId);

    /**
     * 获取待处理的好友请求列表
     */
    List<FriendRequest> getPendingFriendRequests(Long userId);

    /**
     * 获取我发送的好友请求列表
     */
    List<FriendRequest> getSentFriendRequests(Long userId);

    /**
     * 获取好友列表
     */
    List<Friend> getFriendList(Long userId);

    /**
     * 检查是否为好友关系
     */
    boolean isFriend(Long userId, Long friendId);    /**
     * 发送消息（通过用户名）
     */
    Message sendMessageByUsername(Long fromUserId, String toUsername, String content);
    
    /**
     * 发送消息
     */
    Message sendMessage(Long fromUserId, Long toUserId, String content);

    /**
     * 获取与某个用户的聊天消息列表
     */
    List<Message> getChatMessages(Long userId, Long friendId, Integer limit);

    /**
     * 获取未读消息数量
     */
    int getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId, Long userId);
}
