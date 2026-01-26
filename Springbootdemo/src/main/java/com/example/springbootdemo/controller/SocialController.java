package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.entity.Friend;
import com.example.springbootdemo.entity.FriendRequest;
import com.example.springbootdemo.entity.Message;
import com.example.springbootdemo.service.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社交功能控制器
 */
@RestController
@RequestMapping("/social")
@CrossOrigin
public class SocialController {

    @Autowired
    private SocialService socialService;    /**
     * 发送好友请求（通过用户名）
     */
    @PostMapping("/friend-request/send")
    public Result<?> sendFriendRequest(@RequestBody Map<String, Object> params,
                                       @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return Result.error("未登录");
            }
            
            // 支持通过用户名或用户ID发送请求
            Object toUsernameObj = params.get("toUsername");
            Object toUserIdObj = params.get("toUserId");
            
            if (toUsernameObj != null && !toUsernameObj.toString().trim().isEmpty()) {
                // 通过用户名发送
                String toUsername = toUsernameObj.toString().trim();
                socialService.sendFriendRequestByUsername(currentUserId, toUsername);
                return Result.success("好友请求已发送");
            } else if (toUserIdObj != null) {
                // 通过用户ID发送（兼容旧接口）
                Long toUserId = Long.valueOf(toUserIdObj.toString());
                socialService.sendFriendRequest(currentUserId, toUserId);
                return Result.success("好友请求已发送");
            } else {
                return Result.error("请提供目标用户名或用户ID");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 接受好友请求
     */
    @PostMapping("/friend-request/accept/{requestId}")
    public Result<?> acceptFriendRequest(@PathVariable Long requestId,
                                         @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return Result.error("未登录");
            }
            socialService.acceptFriendRequest(requestId, currentUserId);
            return Result.success("已接受好友请求");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 拒绝好友请求
     */
    @PostMapping("/friend-request/reject/{requestId}")
    public Result<?> rejectFriendRequest(@PathVariable Long requestId,
                                         @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return Result.error("未登录");
            }
            socialService.rejectFriendRequest(requestId, currentUserId);
            return Result.success("已拒绝好友请求");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待处理的好友请求列表
     */
    @GetMapping("/friend-request/pending")
    public Result<List<FriendRequest>> getPendingFriendRequests(
            @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return (Result<List<FriendRequest>>) (Result<?>) Result.error("未登录");
            }
            List<FriendRequest> requests = socialService.getPendingFriendRequests(currentUserId);
            return Result.success(requests);
        } catch (Exception e) {
            return (Result<List<FriendRequest>>) (Result<?>) Result.error(e.getMessage());
        }
    }

    /**
     * 获取我发送的好友请求列表
     */
    @GetMapping("/friend-request/sent")
    public Result<List<FriendRequest>> getSentFriendRequests(
            @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return (Result<List<FriendRequest>>) (Result<?>) Result.error("未登录");
            }
            List<FriendRequest> requests = socialService.getSentFriendRequests(currentUserId);
            return Result.success(requests);
        } catch (Exception e) {
            return (Result<List<FriendRequest>>) (Result<?>) Result.error(e.getMessage());
        }
    }

    /**
     * 获取好友列表
     */
    @GetMapping("/friends")
    public Result<List<Friend>> getFriendList(
            @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return (Result<List<Friend>>) (Result<?>) Result.error("未登录");
            }
            List<Friend> friends = socialService.getFriendList(currentUserId);
            return Result.success(friends);
        } catch (Exception e) {
            return (Result<List<Friend>>) (Result<?>) Result.error(e.getMessage());
        }    }

    /**
     * 发送消息（支持通过用户名）
     */
    @PostMapping("/message/send")
    public Result<Message> sendMessage(@RequestBody Map<String, Object> params,
                                       @RequestHeader(value = "User-Id", required = false) Long currentUserId,
                                       @RequestHeader(value = "X-Load-Test", required = false) String loadTestHeader) {
        try {
            if (currentUserId == null) {
                return (Result<Message>) (Result<?>) Result.error("未登录");
            }
            
            String content = params.get("content") != null ? params.get("content").toString() : null;
            if (content == null || content.trim().isEmpty()) {
                return (Result<Message>) (Result<?>) Result.error("消息内容不能为空");
            }
            
            // 支持通过用户名或用户ID发送消息
            Object toUsernameObj = params.get("toUsername");
            Object toUserIdObj = params.get("toUserId");
            
            // 判断是否为压测写入（若 header X-Load-Test=true，则不会持久化写入数据库）
            boolean isLoadTest = false;
            if (loadTestHeader != null && loadTestHeader.equalsIgnoreCase("true")) {
                isLoadTest = true;
            }
            Object testFlag = params.get("test");
            if (testFlag instanceof Boolean && (Boolean) testFlag) {
                isLoadTest = true;
            }

            Message message;
            if (toUsernameObj != null && !toUsernameObj.toString().trim().isEmpty()) {
                // 通过用户名发送
                String toUsername = toUsernameObj.toString().trim();
                message = socialService.sendMessageByUsername(currentUserId, toUsername, content, !isLoadTest);
            } else if (toUserIdObj != null) {
                // 通过用户ID发送（兼容旧接口）
                Long toUserId = Long.valueOf(toUserIdObj.toString());
                message = socialService.sendMessage(currentUserId, toUserId, content, !isLoadTest);
            } else {
                return (Result<Message>) (Result<?>) Result.error("请提供目标用户名或用户ID");
            }
            
            return Result.success(message);
        } catch (Exception e) {
            return (Result<Message>) (Result<?>) Result.error(e.getMessage());
        }
    }

    /**
     * 获取聊天消息列表
     */
    @GetMapping("/message/chat/{friendId}")
    public Result<List<Message>> getChatMessages(@PathVariable Long friendId,
                                                  @RequestParam(required = false, defaultValue = "50") Integer limit,
                                                  @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return (Result<List<Message>>) (Result<?>) Result.error("未登录");
            }
            List<Message> messages = socialService.getChatMessages(currentUserId, friendId, limit);
            return Result.success(messages);
        } catch (Exception e) {
            return (Result<List<Message>>) (Result<?>) Result.error(e.getMessage());
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/message/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(
            @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return (Result<Map<String, Integer>>) (Result<?>) Result.error("未登录");
            }
            int count = socialService.getUnreadCount(currentUserId);
            Map<String, Integer> result = new HashMap<>();
            result.put("count", count);
            return Result.success(result);
        } catch (Exception e) {
            return (Result<Map<String, Integer>>) (Result<?>) Result.error(e.getMessage());
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/message/read/{messageId}")
    public Result<?> markAsRead(@PathVariable Long messageId,
                                @RequestHeader(value = "User-Id", required = false) Long currentUserId) {
        try {
            if (currentUserId == null) {
                return Result.error("未登录");
            }
            socialService.markAsRead(messageId, currentUserId);
            return Result.success("已标记为已读");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
