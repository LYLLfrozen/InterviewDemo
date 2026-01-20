package com.example.springbootdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.entity.Product;
import com.example.springbootdemo.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器（用户端）- 仅展示上架商品
 */
@RestController
@RequestMapping("/products")
public class ProductUserController {

    @Resource
    private ProductService productService;

    /**
     * 获取上架商品列表（用户端）
     * @return Result
     */
    @GetMapping
    public Result<List<Product>> getOnShelfProducts() {
        try {
            List<Product> products = productService.getOnShelfProducts();
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询上架商品（用户端）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return Result
     */
    @GetMapping("/page")
    public Result<Page<Product>> getOnShelfProductsByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<Product> page = productService.getOnShelfProductsByPage(pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询商品失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情（用户端，仅上架商品）
     * @param id 商品ID
     * @return Result
     */
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }
            // 只返回上架的商品
            if (product.getStatus() != 1) {
                return Result.error("商品未上架");
            }
            return Result.success(product);
        } catch (Exception e) {
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }
}
