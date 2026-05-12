package com.coding.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 书籍分类实体类
 *
 * @author system
 * @since 2025-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "book_category")
@ApiModel(value = "BookCategory对象", description = "书籍分类表")
public class BookCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    @Column(name = "parent_id")
    @ApiModelProperty(value = "父分类ID")
    private Long parentId;

    @Column(name = "name")
    @ApiModelProperty(value = "分类名称")
    private String name;

    @Column(name = "sort_order")
    @ApiModelProperty(value = "排序")
    private Integer sortOrder;

    @Column(name = "icon")
    @ApiModelProperty(value = "图标")
    private String icon;

    @Column(name = "status")
    @ApiModelProperty(value = "状态：0-禁用，1-启用")
    private Integer status;

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
