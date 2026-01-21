package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.entity.FriendRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友请求 Mapper
 */
@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequest> {
}
