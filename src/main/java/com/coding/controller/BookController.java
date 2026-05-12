package com.coding.controller;

import com.coding.common.Const;
import com.coding.entity.Book;
import com.coding.service.IBookService;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

/**
 * 书籍控制器
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Api(tags = "书籍管理")
@RequestMapping(Const.API + "book")
@RequiredArgsConstructor
@RestController
public class BookController {

    private final IBookService bookService;

    @ApiOperation("分页查询书籍列表")
    @GetMapping("page")
    public PageResult<Book> page(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long sellerId,
            @Validated RequestPage param) {
        return bookService.page(title, categoryId, status, sellerId, param);
    }

    @ApiOperation("新增书籍")
    @PostMapping("add")
    public R<String> add(@RequestBody @Valid Book book) {
        return bookService.add(book);
    }

    @ApiOperation("更新书籍")
    @PostMapping("update")
    public R<String> update(@RequestBody @Valid Book book) {
        return bookService.update(book);
    }

    @ApiOperation("删除书籍")
    @GetMapping("delete")
    public R<String> delete(@RequestParam Long bookId) {
        return bookService.delete(bookId);
    }

    @ApiOperation("查询书籍详情")
    @GetMapping("detail")
    public R<Book> findById(@RequestParam Long bookId) {
        return bookService.findById(bookId);
    }

    @ApiOperation("上架/下架书籍")
    @PostMapping("updateStatus")
    public R<String> updateStatus(@RequestParam Long bookId, @RequestParam Integer status) {
        return bookService.updateStatus(bookId, status);
    }
}
