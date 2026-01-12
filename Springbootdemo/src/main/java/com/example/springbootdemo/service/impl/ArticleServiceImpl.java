package com.example.springbootdemo.service.impl;

import com.example.springbootdemo.entity.Article;
import com.example.springbootdemo.mapper.ArticleMapper;
import com.example.springbootdemo.service.ArticleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文章服务实现类（仅使用MySQL存储）
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    public ArticleServiceImpl(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public Article createArticle(Article article) {
        // 生成唯一ID
        String id = UUID.randomUUID().toString();
        article.setId(id);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        
        // 如果没有传 position，则设置为0
        if (article.getPosition() == null) {
            article.setPosition(0);
        }
        
        // 保存到 MySQL
        try {
            int rows = articleMapper.insertArticle(article);
            logger.info("Inserted article into MySQL, rows={}", rows);
            return article;
        } catch (Exception ex) {
            logger.error("Failed to save article to MySQL: {}", ex.toString(), ex);
            throw new RuntimeException("创建文章失败", ex);
        }
    }

    @Override
    public Article getArticleById(String id) {
        try {
            return articleMapper.selectArticleById(id);
        } catch (Exception ex) {
            logger.error("Failed to read article from MySQL: {}", ex.toString(), ex);
            return null;
        }
    }

    @Override
    public List<Article> getAllArticles() {
        try {
            return articleMapper.selectAllArticles();
        } catch (Exception ex) {
            logger.error("Failed to read articles from MySQL: {}", ex.toString(), ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Article> getArticlesByUser(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        try {
            return articleMapper.selectArticlesByUser(userId);
        } catch (Exception ex) {
            logger.error("Failed to read user articles from MySQL: {}", ex.toString(), ex);
            return new ArrayList<>();
        }
    }

    @Override
    public Article updateArticle(Article article) {
        if (article.getId() == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        Article existingArticle = getArticleById(article.getId());
        if (existingArticle == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        // 合并非空字段，避免覆盖已有数据
        if (article.getTitle() != null) existingArticle.setTitle(article.getTitle());
        if (article.getContent() != null) existingArticle.setContent(article.getContent());
        if (article.getAuthor() != null) existingArticle.setAuthor(article.getAuthor());
        if (article.getCategory() != null) existingArticle.setCategory(article.getCategory());
        if (article.getPosition() != null) existingArticle.setPosition(article.getPosition());

        existingArticle.setUpdateTime(LocalDateTime.now());
        
        // 更新 MySQL
        try {
            int rows = articleMapper.updateArticle(existingArticle);
            logger.info("Updated article in MySQL, rows={}", rows);
            return existingArticle;
        } catch (Exception ex) {
            logger.error("Failed to update article in MySQL: {}", ex.toString(), ex);
            throw new RuntimeException("更新文章失败", ex);
        }
    }

    @Override
    public boolean deleteArticle(String id) {
        // 删除 MySQL
        try {
            int rows = articleMapper.deleteArticle(id);
            logger.info("Deleted article in MySQL, rows={}", rows);
            return rows > 0;
        } catch (Exception ex) {
            logger.error("Failed to delete article in MySQL: {}", ex.toString(), ex);
            return false;
        }
    }
}
