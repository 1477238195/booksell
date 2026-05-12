package com.coding.mapper;

import com.coding.entity.BookWanted;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * 求购信息Mapper接口
 *
 * @author system
 * @since 2025-01-24
 */
public interface BookWantedMapper extends Mapper<BookWanted> {

    /**
     * 分页查询求购列表（带关联查询）
     */
    List<BookWanted> selectWantedListWithDetails(@Param("bookTitle") String bookTitle,
                                                  @Param("userId") Long userId,
                                                  @Param("status") Integer status);

    /**
     * 根据ID查询求购详情
     */
    BookWanted selectById(@Param("wantedId") Long wantedId);

    /**
     * 插入求购信息（兼容旧数据库）
     */
    int insertWanted(BookWanted bookWanted);

    /**
     * 增加浏览次数
     */
    int increaseViewCount(@Param("wantedId") Long wantedId);
}
