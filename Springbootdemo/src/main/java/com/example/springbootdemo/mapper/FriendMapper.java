package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.entity.Friend;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友关系 Mapper
 */
@Mapper
public interface FriendMapper extends BaseMapper<Friend> {
}
