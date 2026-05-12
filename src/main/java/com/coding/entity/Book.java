package com.coding.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 书籍实体类
 *
 * @author system
 * @since 2025-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "book")
@ApiModel(value = "Book对象", description = "书籍表")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    @ApiModelProperty(value = "书籍ID")
    private Long bookId;

    @Column(name = "title")
    @ApiModelProperty(value = "书名")
    private String title;

    @Column(name = "author")
    @ApiModelProperty(value = "作者")
    private String author;

    @Column(name = "isbn")
    @ApiModelProperty(value = "ISBN编号")
    private String isbn;

    @Column(name = "category_id")
    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    @Column(name = "cover_image")
    @ApiModelProperty(value = "封面图片URL")
    private String coverImage;

    @Column(name = "description")
    @ApiModelProperty(value = "书籍描述")
    private String description;

    @Column(name = "price")
    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @Column(name = "original_price")
    @ApiModelProperty(value = "原价")
    private BigDecimal originalPrice;

    @Column(name = "`condition`")
    @ApiModelProperty(value = "新旧程度：1-全新，2-几乎全新，3-轻微使用痕迹，4-明显使用痕迹")
    private Integer condition;

    @Column(name = "stock")
    @ApiModelProperty(value = "库存数量")
    private Integer stock;

    @Column(name = "seller_id")
    @ApiModelProperty(value = "卖家ID")
    private Long sellerId;

    @Column(name = "status")
    @ApiModelProperty(value = "状态：0-已下架，1-在售，2-已售出")
    private Integer status;

    @Column(name = "view_count")
    @ApiModelProperty(value = "浏览次数")
    private Integer viewCount;

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @Column(name = "deleted")
    @ApiModelProperty(value = "逻辑删除：0-未删除，1-已删除")
    private Integer deleted;

    // 非数据库字段
    @Transient
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @Transient
    @ApiModelProperty(value = "卖家用户名")
    private String sellerName;
}
