package com.example.springbootdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.entity.User;
import com.example.springbootdemo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 新增用户
     * @param user
     * @return
     */
    @PostMapping
    public Result save(@RequestBody User user){
        userService.save(user);
        return Result.success();
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        String errorMsg = userService.register(user);
        if (errorMsg != null) {
            return Result.error(errorMsg);
        }
        return Result.success();
    }    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginForm) {
        String account = loginForm.get("username");
        String password = loginForm.get("password");
        
        String token = userService.login(account, password);
        if (token == null) {
            return Result.error("用户名或密码错误");
        }
          return Result.success(Map.of("token", token));
    }
    
    /**
     * 获取当前登录用户信息
     * @param authorization Authorization header containing the token
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public Result getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error("未登录或 token 无效");
        }
        
        String token = authorization.substring(7); // Remove "Bearer " prefix
        Long userId = userService.getUserIdByToken(token);
        
        if (userId == null) {
            return Result.error("token 已过期或无效");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 返回用户信息，但不返回密码
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 修改用户
     * @param user
     * @return
     */
    @PutMapping
    public Result update(@RequestBody User user){
        userService.updateById(user);
        return Result.success();
    }
    /**
     * 查询单个用户
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
    /**
     * 查询所有用户
     * @return
     */
    @GetMapping
    public Result list() {
        return Result.success(userService.list());
    }
    /**
     * 删除单个用户
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        // 封号处理：不真正删除记录，仅设置 status=1 表示禁用
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(1);
        userService.updateById(user);
        return Result.success();
    }
    /**
     * 分页查询用户
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer pageNum,@RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.page(new Page<>(pageNum, pageSize)));
    }

        /**
         * 修改用户主键 id（oldId -> newId）
         */
    @PutMapping("/change-id")
    public Result changeId(@RequestBody Map<String, Object> ids) {
        try {
            Object oOld = ids.get("oldId");
            Object oNew = ids.get("newId");
            if (oOld == null || oNew == null) {
                return Result.error("oldId 和 newId 必须提供");
            }
            Long oldId = Long.parseLong(oOld.toString());
            Long newId = Long.parseLong(oNew.toString());
            boolean ok = userService.changeId(oldId, newId);
            if (!ok) {
                return Result.error("修改 id 失败，可能 newId 已存在或 oldId 不存在");
            }
            return Result.success();
        } catch (NumberFormatException ex) {
            return Result.error("id 格式错误");
        }
    }
}
