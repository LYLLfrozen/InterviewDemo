package com.example.springbootdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootdemo.entity.User;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.service.UserService;
import jakarta.annotation.Resource;
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
		if (exist != null) return false;
		int updated = baseMapper.changeId(oldId, newId);
		return updated > 0;
	}
	
	private static final String REMEMBERED_LOGIN_KEY = "remembered:login:credentials";
	private static final Duration REMEMBERED_LOGIN_EXPIRATION = Duration.ofDays(30);
	
	@Override
	public void saveRememberedLogin(String username, String password) {
		try {
			stringRedisTemplate.opsForHash().put(REMEMBERED_LOGIN_KEY, "username", username);
			stringRedisTemplate.opsForHash().put(REMEMBERED_LOGIN_KEY, "password", password);
			stringRedisTemplate.expire(REMEMBERED_LOGIN_KEY, REMEMBERED_LOGIN_EXPIRATION);
		} catch (Exception ex) {
			logger.warn("Failed to save remembered login to Redis: {}", ex.toString());
		}
	}
	
	@Override
	public Map<String, String> getRememberedLogin() {
		try {
			Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(REMEMBERED_LOGIN_KEY);
			if (entries == null || entries.isEmpty()) {
				return null;
			}
			Map<String, String> result = new HashMap<>();
			result.put("username", (String) entries.get("username"));
			result.put("password", (String) entries.get("password"));
			return result;
		} catch (Exception ex) {
			logger.warn("Failed to read remembered login from Redis: {}", ex.toString());
			return null;
		}
	}
	
	@Override
	public void clearRememberedLogin() {
		try {
			stringRedisTemplate.delete(REMEMBERED_LOGIN_KEY);
		} catch (Exception ex) {
			logger.warn("Failed to clear remembered login from Redis: {}", ex.toString());
		}
	}

}
