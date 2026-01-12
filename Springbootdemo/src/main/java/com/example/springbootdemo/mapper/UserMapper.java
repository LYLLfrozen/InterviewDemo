package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

	@Update("UPDATE `user` SET id = #{newId} WHERE id = #{oldId}")
	int changeId(@Param("oldId") Long oldId, @Param("newId") Long newId);

}
