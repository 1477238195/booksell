package com.coding.mapper;

import com.coding.entity.BookComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 书籍评论Mapper接口
 */
@Mapper
public interface BookCommentMapper {

    /**
     * 根据书籍ID查询评论列表
     *
     * @param bookId 书籍ID
     * @return 评论列表
     */
    List<BookComment> selectByBookId(@Param("bookId") Long bookId);

    /**
     * 插入评论
     *
     * @param comment 评论实体
     * @return 影响行数
     */
    int insert(BookComment comment);

    /**
     * 根据ID查询评论
     *
     * @param commentId 评论ID
     * @return 评论实体
     */
    BookComment selectById(@Param("commentId") Long commentId);

    /**
     * 根据ID删除评论（逻辑删除）
     *
     * @param commentId 评论ID
     * @return 影响行数
     */
    int deleteById(@Param("commentId") Long commentId);

    /**
     * 根据书籍ID删除所有评论（逻辑删除）
     *
     * @param bookId 书籍ID
     * @return 影响行数
     */
    int deleteByBookId(@Param("bookId") Long bookId);

    /**
     * 根据用户ID删除所有评论（逻辑删除）
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 查询评论数量
     *
     * @param bookId 书籍ID
     * @return 评论数量
     */
    int countByBookId(@Param("bookId") Long bookId);
}
