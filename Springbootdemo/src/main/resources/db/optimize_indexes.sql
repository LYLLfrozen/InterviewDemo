-- 数据库索引优化脚本
-- 针对高并发场景优化查询性能

-- ===================================
-- User 表索引优化
-- ===================================
-- 已有索引：uk_user_username (username), idx_user_email (email)
-- 补充：status 字段用于筛选封号用户
ALTER TABLE `user` ADD INDEX `idx_user_status` (`status`) COMMENT '用户状态索引';

-- ===================================
-- Friend Request 表索引优化
-- ===================================
-- 已有索引：idx_from_user_id, idx_to_user_id, idx_status
-- 补充：组合索引优化查询待处理请求的场景
ALTER TABLE `friend_request` 
    ADD INDEX `idx_to_user_status` (`to_user_id`, `status`, `created_at` DESC) 
    COMMENT '接收方+状态+时间组合索引，优化查询待处理请求';

ALTER TABLE `friend_request` 
    ADD INDEX `idx_from_user_created` (`from_user_id`, `created_at` DESC) 
    COMMENT '发送方+时间组合索引，优化查询已发送请求';

-- ===================================
-- Friend 表索引优化
-- ===================================
-- 已有索引：uk_user_friend (user_id, friend_id), idx_user_id, idx_friend_id
-- 已有组合唯一索引，无需额外优化

-- ===================================
-- Message 表索引优化
-- ===================================
-- 已有索引：idx_from_user_id, idx_to_user_id, idx_timestamp
-- 补充：组合索引优化查询聊天消息和未读消息的场景
ALTER TABLE `message` 
    ADD INDEX `idx_to_user_read` (`to_user_id`, `is_read`) 
    COMMENT '接收方+已读状态组合索引，优化查询未读消息数';

ALTER TABLE `message` 
    ADD INDEX `idx_chat_query` (`from_user_id`, `to_user_id`, `timestamp` DESC) 
    COMMENT '发送方+接收方+时间组合索引，优化聊天消息查询';

-- ===================================
-- Conversation 表索引优化（如有使用）
-- ===================================
ALTER TABLE `conversation` 
    ADD INDEX `idx_user_updated` (`user_id`, `updated_at` DESC) 
    COMMENT '用户+更新时间组合索引';

-- ===================================
-- Message Record 表索引优化（如有使用）
-- ===================================
ALTER TABLE `message_record` 
    ADD INDEX `idx_conversation_created` (`conversation_id`, `created_at` DESC) 
    COMMENT '对话+创建时间组合索引';

-- ===================================
-- Article 表索引优化
-- ===================================
ALTER TABLE `article` 
    ADD INDEX `idx_category_position` (`category`, `position`) 
    COMMENT '分类+位置组合索引，优化按分类查询';

ALTER TABLE `article` 
    ADD INDEX `idx_author_create` (`author`, `create_time` DESC) 
    COMMENT '作者+创建时间组合索引';

-- ===================================
-- Product 表索引优化
-- ===================================
ALTER TABLE `product` 
    ADD INDEX `idx_status_create` (`status`, `create_time` DESC) 
    COMMENT '状态+创建时间组合索引，优化商品列表查询';

-- ===================================
-- 查看当前索引使用情况的 SQL（运行后监控）
-- ===================================
-- SHOW INDEX FROM `user`;
-- SHOW INDEX FROM `friend_request`;
-- SHOW INDEX FROM `friend`;
-- SHOW INDEX FROM `message`;

-- ===================================
-- 性能监控相关（可选，生产环境使用）
-- ===================================
-- 查看慢查询日志
-- SHOW VARIABLES LIKE 'slow_query%';
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1;  -- 查询超过 1 秒记录为慢查询

-- 分析表统计信息（定期执行）
-- ANALYZE TABLE `user`, `friend_request`, `friend`, `message`;
