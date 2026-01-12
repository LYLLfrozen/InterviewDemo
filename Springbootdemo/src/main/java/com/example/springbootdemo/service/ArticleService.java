package com.example.springbootdemo.service;

import com.example.springbootdemo.entity.Article;
import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 创建文章
     * @param article 文章对象
     * @return 创建的文章
     */
    Article createArticle(Article article);

    /**
     * 根据ID获取文章
     * @param id 文章ID
     * @return 文章对象
     */
    Article getArticleById(String id);

    /**
     * 获取所有文章列表（目录）
     * @return 文章列表
     */
    List<Article> getAllArticles();

    /**
     * 获取指定用户的文章列表
     * @param userId 用户ID（字符串形式）
     * @return 文章列表
     */
    List<Article> getArticlesByUser(String userId);

    /**
     * 更新文章
     * @param article 文章对象
     * @return 更新后的文章
     */
    Article updateArticle(Article article);

    /**
     * 删除文章
     * @param id 文章ID
     * @return 是否删除成功
     */
    boolean deleteArticle(String id);
}
