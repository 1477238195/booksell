package com.coding.service;

import com.coding.entity.BookComment;

import java.util.List;

/**
 * 书籍评论Service接口
 */
public interface IBookCommentService {

    /**
     * 根据书籍ID查询评论列表
     *
     * @param bookId 书籍ID
     * @return 评论列表
     */
    List<BookComment> getCommentsByBookId(Long bookId);

    /**
     * 添加评论
     *
     * @param bookId  书籍ID
     * @param userId  用户ID
     * @param content 评论内容
     * @return 是否成功
     */
    boolean addComment(Long bookId, Long userId, String content);

    /**
     * 删除评论（卖家和管理员可用）
     *
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @param userRole  当前用户角色
     * @param bookSellerId 书籍卖家ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId, Long userId, Integer userRole, Long bookSellerId);

    /**
     * 查询评论数量
     *
     * @param bookId 书籍ID
     * @return 评论数量
     */
    int countComments(Long bookId);
}
