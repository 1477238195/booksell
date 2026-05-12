package com.coding.mapper;

import com.coding.entity.Book;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 书籍Mapper
 *
 * @author system
 * @since 2025-01-24
 */
public interface BookMapper extends Mapper<Book> {

    /**
     * 分页查询书籍列表（带关联查询）
     */
    List<Book> selectBookListWithDetails(@Param("title") String title,
                                          @Param("categoryId") Long categoryId,
                                          @Param("status") Integer status,
                                          @Param("sellerId") Long sellerId);

    /**
     * 增加浏览次数
     */
    int increaseViewCount(@Param("bookId") Long bookId);
}
