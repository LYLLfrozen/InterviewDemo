package com.example.springbootdemo.mapper;

import com.example.springbootdemo.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ArticleMapper {
    int insertArticle(Article article);
    Article selectArticleById(@Param("id") String id);
    List<Article> selectAllArticles();
    List<Article> selectArticlesByUser(@Param("userId") String userId);
    int updateArticle(Article article);
    int deleteArticle(@Param("id") String id);
}
