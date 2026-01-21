package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springbootdemo.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
	String login(String account, String password);
	String register(User user);

	/**
	 * 修改用户主键 id（将 oldId 改为 newId），若 newId 已存在或 oldId 不存在则返回 false
	 */
	boolean changeId(Long oldId, Long newId);
	
	/**
	 * 根据用户名查找用户
	 */
	User getUserByUsername(String username);
	
	/**
	 * 根据 token 获取用户 ID
	 * @param token 登录 token
	 * @return 用户 ID，如果 token 无效则返回 null
	 */
	Long getUserIdByToken(String token);
}
