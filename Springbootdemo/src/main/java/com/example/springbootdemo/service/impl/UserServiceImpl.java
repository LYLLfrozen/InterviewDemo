package com.example.springbootdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootdemo.entity.User;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	@Resource
	private StringRedisTemplate stringRedisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public String login(String account, String password) {
		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
		wrapper.and(w -> w.eq(User::getUsername, account).or().eq(User::getEmail, account));
		User user = getOne(wrapper, false);
		if (user == null || !password.equals(user.getPassword())) {
			return null;
		}
		// 如果用户已被封号/禁用，拒绝登录
		if (user.getStatus() != null && user.getStatus() == 1) {
			return null;
		}
		String token = UUID.randomUUID().toString().replace("-", "");
		try {
			stringRedisTemplate.opsForValue().set("login:token:" + token, String.valueOf(user.getId()), Duration.ofMinutes(30));
		} catch (Exception ex) {
			logger.warn("Failed to write login token to Redis (continuing without Redis): {}", ex.toString());
		}
		return token;
	}

	@Override
	@CacheEvict(value = "user", key = "'username:' + #user.username")
	public String register(User user) {
		LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(User::getUsername, user.getUsername());
		if (count(queryWrapper) > 0) {
			return "用户名已存在";
		}
		
		if (user.getEmail() != null && !user.getEmail().isEmpty()) {
			LambdaQueryWrapper<User> emailWrapper = new LambdaQueryWrapper<>();
			emailWrapper.eq(User::getEmail, user.getEmail());
			if (count(emailWrapper) > 0) {
				return "邮箱已注册";
			}
		}

		// 按当前最大 id 顺序分配新的 id（保证顺序增长）
		Object maxObj = baseMapper.selectObjs(new QueryWrapper<User>().select("MAX(id) as max_id")).stream().findFirst().orElse(null);
		long maxId = 0L;
		if (maxObj instanceof Number) {
			maxId = ((Number) maxObj).longValue();
		} else if (maxObj != null) {
			try {
				maxId = Long.parseLong(maxObj.toString());
			} catch (NumberFormatException ignored) {
			}
		}
		user.setId(maxId + 1);
		// 默认状态为正常（0）
		if (user.getStatus() == null) {
			user.setStatus(0);
		}

		save(user);
		return null;
	}

	@Override
	public boolean changeId(Long oldId, Long newId) {
		if (oldId == null || newId == null) return false;
		// oldId 必须存在
		User oldUser = getById(oldId);
		if (oldUser == null) return false;
		// newId 不能已存在
		User exist = getById(newId);
		if (exist != null) return false;		int updated = baseMapper.changeId(oldId, newId);
		return updated > 0;
	}

	@Override
	@Cacheable(value = "user", key = "'username:' + #username", unless = "#result == null")
	public User getUserByUsername(String username) {
		if (username == null || username.trim().isEmpty()) {		return null;
		}
		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(User::getUsername, username);
		return getOne(wrapper, false);
	}

	@Override
	public Long getUserIdByToken(String token) {
		if (token == null || token.isEmpty()) {
			return null;
		}
		try {
			String userIdStr = stringRedisTemplate.opsForValue().get("login:token:" + token);
			if (userIdStr == null) {
				return null;
			}
			return Long.parseLong(userIdStr);
		} catch (Exception ex) {
			logger.warn("Failed to get userId from token: {}", ex.toString());
			return null;
		}
	}

	@Override
	public boolean invalidateUserSessions(Long userId) {
		if (userId == null) return false;
		try {
			// 获取所有 login token 键（格式：login:token:<token>）并删除值等于 userId 的键
			java.util.Set<String> keys = stringRedisTemplate.keys("login:token:*");
			if (keys == null || keys.isEmpty()) return true;
			for (String key : keys) {
				try {
					String val = stringRedisTemplate.opsForValue().get(key);
					if (val != null && val.equals(String.valueOf(userId))) {
						stringRedisTemplate.delete(key);
					}
				} catch (Exception ex) {
					logger.warn("Failed processing redis key {}: {}", key, ex.toString());
				}
			}
			return true;
		} catch (Exception ex) {
			logger.warn("Failed to invalidate user sessions for {}: {}", userId, ex.toString());
			return false;
		}
	}

	@Override
	public boolean isUserOnline(Long userId) {
		if (userId == null) return false;
		try {
			java.util.Set<String> keys = stringRedisTemplate.keys("login:token:*");
			if (keys == null || keys.isEmpty()) return false;
			for (String key : keys) {
				try {
					String val = stringRedisTemplate.opsForValue().get(key);
					if (val != null && val.equals(String.valueOf(userId))) {
						return true;
					}
				} catch (Exception ex) {
					logger.warn("Failed processing redis key {}: {}", key, ex.toString());
				}
			}
			return false;
		} catch (Exception ex) {
			logger.warn("Failed to check online status for {}: {}", userId, ex.toString());
			return false;
		}
	}

}
