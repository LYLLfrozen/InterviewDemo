package com.example.springbootdemo.service.impl;

import com.example.springbootdemo.entity.Article;
import com.example.springbootdemo.mapper.ArticleMapper;
import com.example.springbootdemo.service.ArticleService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文章服务实现类
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleMapper articleMapper;
    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);
    
    private static final String ARTICLE_KEY_PREFIX = "article:";
    private static final String ARTICLE_ORDER_KEY = "article:order"; // 有序集合，score 用于 position（数字越小越靠前）

    public ArticleServiceImpl(RedisTemplate<String, Object> redisTemplate, ArticleMapper articleMapper) {
        this.redisTemplate = redisTemplate;
        this.articleMapper = articleMapper;
    }

    @Override
    public Article createArticle(Article article) {
        // 生成唯一ID
        String id = UUID.randomUUID().toString();
        article.setId(id);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        // 如果没有传 position，则默认放到末尾（score = size + 1）
        try {
            Long size = redisTemplate.opsForZSet().size(ARTICLE_ORDER_KEY);
            long pos = (size == null ? 0L : size.longValue()) + 1L;
            if (article.getPosition() == null) {
                article.setPosition((int) pos);
            }
        } catch (Exception ex) {
            // 忽略，后面仍会尝试写入
        }
        
        // 保存文章到Redis（容错）
        String key = ARTICLE_KEY_PREFIX + id;
        try {
            redisTemplate.opsForValue().set(key, article);
            // 将文章ID添加到有序集合中，score 使用 position
            double score = article.getPosition() != null ? article.getPosition() : System.currentTimeMillis();
            redisTemplate.opsForZSet().add(ARTICLE_ORDER_KEY, id, score);
        } catch (Exception ex) {
            logger.warn("Failed to save article to Redis (continuing): {}", ex.toString());
        }
        
        // 保存到 MySQL
        try {
            int rows = articleMapper.insertArticle(article);
            logger.info("Inserted article into MySQL, rows={}", rows);
        } catch (Exception ex) {
            logger.warn("Failed to save article to MySQL: {}", ex.toString(), ex);
        }
        
        return article;
    }

    @Override
    public Article getArticleById(String id) {
        String key = ARTICLE_KEY_PREFIX + id;
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj != null) {
                return (Article) obj;
            }
        } catch (Exception ex) {
            logger.warn("Failed to read article from Redis: {}", ex.toString());
        }
        
        // 如果 Redis 没有，则查 MySQL
        try {
            return articleMapper.selectArticleById(id);
        } catch (Exception ex) {
            logger.warn("Failed to read article from MySQL: {}", ex.toString());
            return null;
        }
    }

    @Override
    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<>();
        try {
            // 先尝试从有序集合按 score(即 position) 顺序读取
            Set<Object> articleIds = redisTemplate.opsForZSet().range(ARTICLE_ORDER_KEY, 0, -1);
            if (articleIds != null && !articleIds.isEmpty()) {
                for (Object id : articleIds) {
                    Article article = getArticleById(id.toString());
                    if (article != null) {
                        articles.add(article);
                    }
                }
                return articles;
            }
        } catch (Exception ex) {
            logger.warn("Failed to read article order from Redis: {}", ex.toString());
            // 继续回退到逐个读取（虽然可能无序）
        }
        
        // 如果 Redis 没有，则查 MySQL
        if (articles.isEmpty()) {
            try {
                return articleMapper.selectAllArticles();
            } catch (Exception ex) {
                logger.warn("Failed to read articles from MySQL: {}", ex.toString());
            }
        }
        
        // 回退：遍历有可能的 keys（兼容旧数据），这里尝试读取所有以 ARTICLE_KEY_PREFIX 开头的 keys 不采用扫描以保持简单；如果失败返回空
        try {
            // 无法安全列举所有 key 的实现，这里仅返回空列表以避免抛出异常给控制器
            return articles;
        } catch (Exception ex) {
            logger.warn("Fallback read failed: {}", ex.toString());
            return articles;
        }
    }

    @Override
    public List<Article> getArticlesByUser(String userId) {
        List<Article> all = getAllArticles();
        if (userId == null) return new ArrayList<>();
        List<Article> filtered = new ArrayList<>();
        for (Article a : all) {
            if (a == null) continue;
            // author 字段保存了作者 id 或用户名，按实际存储比较
            if (userId.equals(String.valueOf(a.getAuthor()))) {
                filtered.add(a);
            }
        }
        
        if (filtered.isEmpty()) {
            try {
                return articleMapper.selectArticlesByUser(userId);
            } catch (Exception ex) {
                logger.warn("Failed to read user articles from MySQL: {}", ex.toString());
            }
        }
        
        return filtered;
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
        
        String key = ARTICLE_KEY_PREFIX + existingArticle.getId();
        redisTemplate.opsForValue().set(key, existingArticle);

        // 如果 position 有设置，则更新有序集合的 score
        try {
            if (existingArticle.getPosition() != null) {
                redisTemplate.opsForZSet().add(ARTICLE_ORDER_KEY, existingArticle.getId(), existingArticle.getPosition());
            }
        } catch (Exception ex) {
            logger.warn("Failed to update article order in Redis: {}", ex.toString());
        }
        
        // 更新 MySQL
        try {
            int rows = articleMapper.updateArticle(existingArticle);
            logger.info("Updated article in MySQL, rows={}", rows);
        } catch (Exception ex) {
            logger.warn("Failed to update article in MySQL: {}", ex.toString(), ex);
        }
        
        return existingArticle;
    }

    @Override
    public boolean deleteArticle(String id) {
        String key = ARTICLE_KEY_PREFIX + id;
        Boolean deleted = redisTemplate.delete(key);
        
        try {
            redisTemplate.opsForZSet().remove(ARTICLE_ORDER_KEY, id);
        } catch (Exception ex) {
            // 忽略
        }
        
        // 删除 MySQL
        try {
            int rows = articleMapper.deleteArticle(id);
            logger.info("Deleted article in MySQL, rows={}", rows);
        } catch (Exception ex) {
            logger.warn("Failed to delete article in MySQL: {}", ex.toString(), ex);
        }
        
        if (Boolean.TRUE.equals(deleted)) {
            return true;
        }
        
        return false;
    }
}
