package com.example.springbootdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootdemo.entity.Product;
import com.example.springbootdemo.mapper.ProductMapper;
import com.example.springbootdemo.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Product createProduct(Product product) {
        product.setCreateTime(LocalDateTime.now());
        if (product.getStatus() == null) {
            product.setStatus(0); // 默认下架
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        try {
            productMapper.insert(product);
            logger.info("Created product: {}", product.getId());
            return product;
        } catch (Exception ex) {
            logger.error("Failed to create product: {}", ex.toString(), ex);
            throw new RuntimeException("创建商品失败", ex);
        }
    }

    @Override
    public Product updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        Product existingProduct = productMapper.selectById(product.getId());
        if (existingProduct == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        try {
            productMapper.updateById(product);
            logger.info("Updated product: {}", product.getId());
            return productMapper.selectById(product.getId());
        } catch (Exception ex) {
            logger.error("Failed to update product: {}", ex.toString(), ex);
            throw new RuntimeException("更新商品失败", ex);
        }
    }

    @Override
    public boolean deleteProduct(Long id) {
        try {
            int rows = productMapper.deleteById(id);
            logger.info("Deleted product: {}, rows={}", id, rows);
            return rows > 0;
        } catch (Exception ex) {
            logger.error("Failed to delete product: {}", ex.toString(), ex);
            return false;
        }
    }

    @Override
    public Product getProductById(Long id) {
        try {
            return productMapper.selectById(id);
        } catch (Exception ex) {
            logger.error("Failed to get product: {}", ex.toString(), ex);
            return null;
        }
    }

    @Override
    public List<Product> getAllProducts() {
        try {
            return productMapper.selectList(null);
        } catch (Exception ex) {
            logger.error("Failed to get all products: {}", ex.toString(), ex);
            throw new RuntimeException("获取商品列表失败", ex);
        }
    }

    @Override
    public Page<Product> getProductsByPage(Integer pageNum, Integer pageSize) {
        try {
            Page<Product> page = new Page<>(pageNum, pageSize);
            return productMapper.selectPage(page, null);
        } catch (Exception ex) {
            logger.error("Failed to get products by page: {}", ex.toString(), ex);
            throw new RuntimeException("分页查询商品失败", ex);
        }
    }

    @Override
    public boolean onShelf(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        product.setStatus(1);
        try {
            int rows = productMapper.updateById(product);
            logger.info("Product on shelf: {}, rows={}", id, rows);
            return rows > 0;
        } catch (Exception ex) {
            logger.error("Failed to put product on shelf: {}", ex.toString(), ex);
            return false;
        }
    }

    @Override
    public boolean offShelf(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        product.setStatus(0);
        try {
            int rows = productMapper.updateById(product);
            logger.info("Product off shelf: {}, rows={}", id, rows);
            return rows > 0;
        } catch (Exception ex) {
            logger.error("Failed to take product off shelf: {}", ex.toString(), ex);
            return false;
        }
    }

    @Override
    public List<Product> getOnShelfProducts() {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1);
            return productMapper.selectList(queryWrapper);
        } catch (Exception ex) {
            logger.error("Failed to get on-shelf products: {}", ex.toString(), ex);
            throw new RuntimeException("获取上架商品列表失败", ex);
        }
    }

    @Override
    public Page<Product> getOnShelfProductsByPage(Integer pageNum, Integer pageSize) {
        try {
            Page<Product> page = new Page<>(pageNum, pageSize);
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 1);
            return productMapper.selectPage(page, queryWrapper);
        } catch (Exception ex) {
            logger.error("Failed to get on-shelf products by page: {}", ex.toString(), ex);
            throw new RuntimeException("分页查询上架商品失败", ex);
        }
    }
}
