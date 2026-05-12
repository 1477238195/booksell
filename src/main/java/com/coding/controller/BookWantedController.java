package com.coding.controller;

import com.coding.common.Const;
import com.coding.entity.BookWanted;
import com.coding.service.IBookWantedService;
import com.coding.utils.HttpKit;
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
 * 求购信息控制器
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Api(tags = "求购信息管理")
@RequestMapping(Const.API + "bookWanted")
@RequiredArgsConstructor
@RestController
public class BookWantedController {

    private final IBookWantedService bookWantedService;

    @ApiOperation("分页查询求购列表")
    @GetMapping("page")
    public PageResult<BookWanted> page(
            @RequestParam(required = false) String bookTitle,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @Validated RequestPage param) {
        return bookWantedService.page(bookTitle, userId, status, param);
    }

    @ApiOperation("我的求购")
    @GetMapping("myWanted")
    public PageResult<BookWanted> myWanted(
            @RequestParam(required = false) Integer status,
            @Validated RequestPage param) {
        Long userId = HttpKit.getUserId();
        return bookWantedService.page(null, userId, status, param);
    }

    @ApiOperation("发布求购")
    @PostMapping("add")
    public R<String> add(@RequestBody @Valid BookWanted bookWanted) {
        return bookWantedService.add(bookWanted);
    }

    @ApiOperation("更新求购")
    @PostMapping("update")
    public R<String> update(@RequestBody @Valid BookWanted bookWanted) {
        return bookWantedService.update(bookWanted);
    }

    @ApiOperation("删除求购")
    @GetMapping("delete")
    public R<String> delete(@RequestParam Long wantedId) {
        return bookWantedService.delete(wantedId);
    }

    @ApiOperation("查询求购详情")
    @GetMapping("detail")
    public R<BookWanted> findById(@RequestParam Long wantedId) {
        return bookWantedService.findById(wantedId);
    }

    @ApiOperation("更新求购状态")
    @PostMapping("updateStatus")
    public R<String> updateStatus(@RequestParam Long wantedId, @RequestParam Integer status) {
        return bookWantedService.updateStatus(wantedId, status);
    }
}
