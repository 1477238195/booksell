package com.coding.controller;

import com.coding.common.Const;
import com.coding.entity.Book;
import com.coding.entity.BookWanted;
import com.coding.service.IRecommendService;
import com.coding.utils.HttpKit;
import com.coding.utils.PageResult;
import com.coding.utils.RequestPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页混合推荐接口。
 */
@Api(tags = "推荐")
@RequestMapping(Const.API + "recommend")
@RequiredArgsConstructor
@RestController
public class RecommendController {

    private final IRecommendService recommendService;

    @ApiOperation("首页推荐书籍（TF-IDF + 协同过滤 + 热门）")
    @GetMapping("home/books")
    public PageResult<Book> homeBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @Validated RequestPage param) {
        Long viewerUserId = HttpKit.getUserId();
        return recommendService.recommendHomeBooks(title, categoryId, status, viewerUserId, param);
    }

    @ApiOperation("首页推荐求购")
    @GetMapping("home/wanted")
    public PageResult<BookWanted> homeWanted(
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) Integer status,
            @Validated RequestPage param) {
        Long viewerUserId = HttpKit.getUserId();
        return recommendService.recommendHomeWanted(bookTitle, status, viewerUserId, param);
    }
}
