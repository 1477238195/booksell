package com.coding.service;

import com.coding.entity.BookWanted;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;

/**
 * 求购信息Service接口
 *
 * @author system
 * @since 2025-01-24
 */
public interface IBookWantedService {

    /**
     * 分页查询求购列表
     */
    PageResult<BookWanted> page(String bookTitle, Long userId, Integer status, RequestPage param);

    /**
     * 发布求购
     */
    R<String> add(BookWanted bookWanted);

    /**
     * 更新求购
     */
    R<String> update(BookWanted bookWanted);

    /**
     * 删除求购
     */
    R<String> delete(Long wantedId);

    /**
     * 查询求购详情
     */
    R<BookWanted> findById(Long wantedId);

    /**
     * 更新状态
     */
    R<String> updateStatus(Long wantedId, Integer status);
}
