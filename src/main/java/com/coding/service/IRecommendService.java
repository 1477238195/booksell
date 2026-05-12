package com.coding.service;

import com.coding.entity.Book;
import com.coding.entity.BookWanted;
import com.coding.utils.PageResult;
import com.coding.utils.RequestPage;

/**
 * 首页混合推荐：TF-IDF 内容 + 用户协同过滤 + 冷启动热门。
 */
public interface IRecommendService {

    PageResult<Book> recommendHomeBooks(String title, Long categoryId, Integer status,
                                        Long viewerUserId, RequestPage param);

    PageResult<BookWanted> recommendHomeWanted(String bookTitle, Integer status,
                                                 Long viewerUserId, RequestPage param);
}
