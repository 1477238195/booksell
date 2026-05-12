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
 * 书籍评论实体类
 *
 * @author system
 * @since 2025-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(name = "book_comment")
@ApiModel(value = "BookComment对象", description = "书籍评论表")
public class BookComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    @ApiModelProperty(value = "评论ID")
    private Long commentId;

    @Column(name = "book_id")
    @ApiModelProperty(value = "书籍ID")
    private Long bookId;

    @Column(name = "user_id")
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @Column(name = "content")
    @ApiModelProperty(value = "评论内容")
    private String content;

    @Column(name = "create_time")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @Column(name = "deleted")
    @ApiModelProperty(value = "逻辑删除：0-未删除，1-已删除")
    private Integer deleted;

    // 非数据库字段
    @Transient
    @ApiModelProperty(value = "用户名")
    private String username;

    @Transient
    @ApiModelProperty(value = "用户角色：0-普通用户，1-卖家，2-管理员")
    private Integer role;
}
