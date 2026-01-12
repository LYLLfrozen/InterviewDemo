package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.service.ApiKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * API Key 管理控制器
 * 提供 API Key 的配置接口
 */
@RestController
@RequestMapping("/api-key")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * 设置 DashScope API Key
     * @param requestBody 包含 apiKey 的请求体
     * @return 操作结果
     */
    @PostMapping("/dashscope")
    public Result<String> setDashscopeApiKey(@RequestBody Map<String, String> requestBody) {
        try {
            String apiKey = requestBody.get("apiKey");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return Result.error("API Key 不能为空");
            }
            apiKeyService.setDashscopeApiKey(apiKey);
            return Result.success("API Key 设置成功");
        } catch (Exception e) {
            return Result.error("设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取 API Key 状态（不返回完整 key，只返回是否已配置）
     * @return 配置状态
     */
    @GetMapping("/dashscope/status")
    public Result<Map<String, Object>> getDashscopeApiKeyStatus() {
        try {
            boolean hasKey = apiKeyService.hasApiKey();
            String apiKey = apiKeyService.getDashscopeApiKey();
            
            Map<String, Object> status = new HashMap<>();
            status.put("configured", hasKey);
            
            // 如果已配置，只显示部分 key（前后各显示4个字符）
            if (hasKey && apiKey != null && apiKey.length() > 8) {
                String maskedKey = apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
                status.put("maskedKey", maskedKey);
            } else if (hasKey) {
                status.put("maskedKey", "sk-****");
            }
            
            return Result.success(status);
        } catch (Exception e) {
            return Result.error("获取状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除 API Key
     * @return 操作结果
     */
    @DeleteMapping("/dashscope")
    public Result<String> deleteDashscopeApiKey() {
        try {
            apiKeyService.deleteDashscopeApiKey();
            return Result.success("API Key 已删除");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
