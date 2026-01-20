package com.example.springbootdemo.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.entity.Product;
import com.example.springbootdemo.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器（管理端）
 */
@RestController
@RequestMapping("/admin/products")
public class ProductController {

    @Resource
    private ProductService productService;

    /**
     * 新增商品
     * @param product 商品对象
     * @return Result
     */
    @PostMapping
    public Result<Product> createProduct(@RequestBody Product product) {
        try {
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return Result.error("商品名称不能为空");
            }
            if (product.getPrice() == null || product.getPrice().doubleValue() < 0) {
                return Result.error("商品价格不能为空或负数");
            }
            Product createdProduct = productService.createProduct(product);
            return Result.success(createdProduct);
        } catch (Exception e) {
            return Result.error("创建商品失败: " + e.getMessage());
        }
    }

    /**
     * 修改商品
     * @param id 商品ID
     * @param product 商品对象
     * @return Result
     */
    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            product.setId(id);
            Product updatedProduct = productService.updateProduct(product);
            return Result.success(updatedProduct);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新商品失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品
     * @param id 商品ID
     * @return Result
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                return Result.success(true);
            } else {
                return Result.error("删除商品失败");
            }
        } catch (Exception e) {
            return Result.error("删除商品失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品
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
            return Result.success(product);
        } catch (Exception e) {
            return Result.error("获取商品失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有商品（管理端）
     * @return Result
     */
    @GetMapping
    public Result<List<Product>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return Result.success(products);
        } catch (Exception e) {
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询所有商品（管理端）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return Result
     */
    @GetMapping("/page")
    public Result<Page<Product>> getProductsByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            Page<Product> page = productService.getProductsByPage(pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询商品失败: " + e.getMessage());
        }
    }

    /**
     * 上架商品
     * @param id 商品ID
     * @return Result
     */
    @PutMapping("/{id}/on-shelf")
    public Result<Boolean> onShelf(@PathVariable Long id) {
        try {
            boolean success = productService.onShelf(id);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("上架商品失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("上架商品失败: " + e.getMessage());
        }
    }

    /**
     * 下架商品
     * @param id 商品ID
     * @return Result
     */
    @PutMapping("/{id}/off-shelf")
    public Result<Boolean> offShelf(@PathVariable Long id) {
        try {
            boolean success = productService.offShelf(id);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("下架商品失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("下架商品失败: " + e.getMessage());
        }
    }
}
