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
	 * 保存记住的登录信息到Redis
	 */
	void saveRememberedLogin(String username, String password);
	
	/**
	 * 获取记住的登录信息
	 */
	Map<String, String> getRememberedLogin();
	
	/**
	 * 清除记住的登录信息
	 */
	void clearRememberedLogin();
}
