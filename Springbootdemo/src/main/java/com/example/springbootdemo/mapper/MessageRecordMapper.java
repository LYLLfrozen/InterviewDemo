package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.entity.MessageRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageRecordMapper extends BaseMapper<MessageRecord> {
}
