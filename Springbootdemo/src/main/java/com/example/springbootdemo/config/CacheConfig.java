package com.example.springbootdemo.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置 - 支持高并发场景的缓存策略
 * 为不同的业务场景配置不同的缓存过期时间
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Redis 缓存管理器
     * 针对不同的缓存区域设置不同的过期时间
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 配置 ObjectMapper 支持 Java 8 时间类型（Java 17 也使用同样的时间 API）
        ObjectMapper objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 支持 LocalDateTime, LocalDate 等
        objectMapper.registerModule(new JavaTimeModule());
        // 配置类型信息，支持多态
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        // 使用配置好的 ObjectMapper 创建序列化器
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 默认缓存配置：10 分钟过期
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // 为不同的缓存区域设置不同的过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户信息缓存：30 分钟（高频读取，低频变更）
        cacheConfigurations.put("user", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 好友列表缓存：15 分钟（中频读取，中频变更）
        cacheConfigurations.put("friendList", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 好友请求缓存：5 分钟（高频变更）
        cacheConfigurations.put("friendRequest", 
                defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 聊天消息缓存：3 分钟（实时性要求高）
        cacheConfigurations.put("chatMessages", 
                defaultConfig.entryTtl(Duration.ofMinutes(3)));
        
        // 未读消息数缓存：1 分钟（需要快速更新）
        cacheConfigurations.put("unreadCount", 
                defaultConfig.entryTtl(Duration.ofMinutes(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
