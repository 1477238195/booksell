package com.coding.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 求购信息实体类
 *
 * @author system
 * @since 2025-01-24
 */
@Data
@Accessors(chain = true)
@Table(name = "book_wanted")
public class BookWanted implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 求购ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wanted_id")
    private Long wantedId;

    /**
     * 用户ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 求购书名
     */
    @Column(name = "book_title")
    private String bookTitle;

    /**
     * 作者
     */
    @Column(name = "author")
    private String author;

    /**
     * ISBN
     */
    @Column(name = "isbn")
    private String isbn;

    /**
     * 封面图片URL
     */
    @Column(name = "cover_image")
    private String coverImage;

    /**
     * 求购说明
     */
    @Column(name = "description")
    private String description;

    /**
     * 期望最高价格
     */
    @Column(name = "max_price")
    private BigDecimal maxPrice;

    /**
     * 状态：0-已关闭，1-求购中，2-已找到
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 浏览次数
     */
    @Column(name = "view_count")
    private Integer viewCount;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @Column(name = "deleted")
    private Integer deleted;

    // 关联查询字段
    @Transient
    private String username;
}
