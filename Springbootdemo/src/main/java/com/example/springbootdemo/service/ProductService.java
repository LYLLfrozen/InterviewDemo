package com.example.springbootdemo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 新增商品
     * @param product 商品对象
     * @return 创建的商品
     */
    Product createProduct(Product product);

    /**
     * 修改商品
     * @param product 商品对象
     * @return 更新后的商品
     */
    Product updateProduct(Product product);

    /**
     * 删除商品
     * @param id 商品ID
     * @return 是否删除成功
     */
    boolean deleteProduct(Long id);

    /**
     * 根据ID获取商品
     * @param id 商品ID
     * @return 商品对象
     */
    Product getProductById(Long id);

    /**
     * 获取所有商品（管理端）
     * @return 商品列表
     */
    List<Product> getAllProducts();

    /**
     * 分页查询所有商品（管理端）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<Product> getProductsByPage(Integer pageNum, Integer pageSize);

    /**
     * 上架商品
     * @param id 商品ID
     * @return 是否成功
     */
    boolean onShelf(Long id);

    /**
     * 下架商品
     * @param id 商品ID
     * @return 是否成功
     */
    boolean offShelf(Long id);

    /**
     * 获取上架商品列表（用户端）
     * @return 商品列表
     */
    List<Product> getOnShelfProducts();

    /**
     * 分页查询上架商品（用户端）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<Product> getOnShelfProductsByPage(Integer pageNum, Integer pageSize);
}
