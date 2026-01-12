package com.example.springbootdemo.controller;

import com.example.springbootdemo.common.Result;
import com.example.springbootdemo.entity.Article;
import com.example.springbootdemo.service.ArticleService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 */
@RestController
@RequestMapping("/mainPage")
public class ArticleController {

    private final ArticleService articleService;
    private final StringRedisTemplate stringRedisTemplate;

    public ArticleController(ArticleService articleService, StringRedisTemplate stringRedisTemplate) {
        this.articleService = articleService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 获取所有文章列表（目录）
     */
    @GetMapping
    public Result<List<Article>> getAllArticles(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "token", required = false) String tokenHeader) {
        try {
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            } else if (tokenHeader != null && !tokenHeader.isEmpty()) {
                token = tokenHeader;
            }

            String userId = null;
            if (token != null) {
                try {
                    Object o = stringRedisTemplate.opsForValue().get("login:token:" + token);
                    if (o != null) userId = o.toString();
                } catch (Exception ex) {
                    // 忽略 Redis 读取失败，继续返回全部文章
                }
            }

            // 如果有userId，尝试获取该用户的文章；如果为空则返回所有文章
            if (userId != null) {
                List<Article> userArticles = articleService.getArticlesByUser(userId);
                if (userArticles != null && !userArticles.isEmpty()) {
                    return Result.success(userArticles);
                }
            }

            // 没有userId或用户没有文章时，返回所有文章
            List<Article> articles = articleService.getAllArticles();
            return Result.success(articles);
        } catch (Exception e) {
            return Result.error("获取文章列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取文章详情
     */
    @GetMapping("/{id}")
    public Result<Article> getArticleById(@PathVariable String id) {
        try {
            Article article = articleService.getArticleById(id);
            if (article == null) {
                return Result.error("文章不存在");
            }
            return Result.success(article);
        } catch (Exception e) {
            return Result.error("获取文章失败: " + e.getMessage());
        }
    }

    /**
     * 创建文章
     */
    @PostMapping
    public Result<Article> createArticle(@RequestHeader(value = "Authorization", required = false) String authorization,
                                         @RequestHeader(value = "token", required = false) String tokenHeader,
                                         @RequestBody Article article) {
        try {
            if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
                return Result.error("文章标题不能为空");
            }
            if (article.getContent() == null || article.getContent().trim().isEmpty()) {
                return Result.error("文章内容不能为空");
            }
            // 如果请求带 token，则尝试设置文章作者为当前用户 ID
            String token = null;
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.substring(7);
            } else if (tokenHeader != null && !tokenHeader.isEmpty()) {
                token = tokenHeader;
            }
            if (token != null) {
                try {
                    Object o = stringRedisTemplate.opsForValue().get("login:token:" + token);
                    if (o != null) {
                        article.setAuthor(o.toString());
                    }
                } catch (Exception ex) {
                    // 忽略 Redis 读取失败，继续创建但不设置作者
                }
            }

            Article createdArticle = articleService.createArticle(article);
            return Result.success(createdArticle);
        } catch (Exception e) {
            return Result.error("创建文章失败: " + e.getMessage());
        }
    }

    /**
     * 更新文章
     */
    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable String id, @RequestBody Article article) {
        try {
            article.setId(id);
            Article updatedArticle = articleService.updateArticle(article);
            return Result.success(updatedArticle);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新文章失败: " + e.getMessage());
        }
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteArticle(@PathVariable String id) {
        try {
            boolean deleted = articleService.deleteArticle(id);
            if (deleted) {
                return Result.success(true);
            } else {
                return Result.error("删除文章失败");
            }
        } catch (Exception e) {
            return Result.error("删除文章失败: " + e.getMessage());
        }
    }
}
