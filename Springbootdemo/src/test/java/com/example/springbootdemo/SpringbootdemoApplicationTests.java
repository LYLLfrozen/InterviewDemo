package com.example.springbootdemo;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.example.springbootdemo.entity.User;
import com.example.springbootdemo.mapper.UserMapper;
import com.example.springbootdemo.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class SpringbootdemoApplicationTests {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;

    @Test
    void contextLoads() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userMapper.selectList(null);
        Assert.isTrue(5 == userList.size(), "");
        userList.forEach(System.out::println);
    }

    @Test
    void contextService(){
        List<User> userList = userService.list();
        userList.forEach(System.out::println);
    }

}
