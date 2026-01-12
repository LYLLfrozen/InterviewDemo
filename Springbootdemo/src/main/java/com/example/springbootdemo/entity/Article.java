package com.example.springbootdemo.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文章ID
     */
    private String id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 作者
     */
    private String author;

    /**
     * 分类
     */
    private String category;

    /**
     * 列表中的位置（用于排序，数字越小越靠前）
     */
    private Integer position;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
