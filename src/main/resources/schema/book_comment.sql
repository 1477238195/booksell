-- 创建书籍评论表
CREATE TABLE IF NOT EXISTS book_comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
    book_id BIGINT NOT NULL COMMENT '书籍ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_book_id (book_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_comment_book FOREIGN KEY (book_id) REFERENCES book(book_id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书籍评论表';
