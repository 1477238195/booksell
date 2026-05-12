package com.coding.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛主题帖
 */
@Data
@Accessors(chain = true)
@Table(name = "forum_topic")
public class ForumTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "reply_count")
    private Integer replyCount;

    @Column(name = "last_reply_at")
    private LocalDateTime lastReplyAt;

    @Column(name = "last_reply_user_id")
    private Long lastReplyUserId;

    @Column(name = "pinned")
    private Integer pinned;

    @Column(name = "locked")
    private Integer locked;

    @Column(name = "deleted")
    private Integer deleted;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /* ---------- 列表/详情关联 ---------- */
    @Transient
    private String authorName;

    @Transient
    private String bookTitle;

    @Transient
    private String bookCover;

    @Transient
    private String lastReplyAuthorName;
}
