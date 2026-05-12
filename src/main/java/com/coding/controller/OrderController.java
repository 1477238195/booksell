package com.coding.controller;

import com.coding.common.Const;
import com.coding.entity.Order;
import com.coding.service.IOrderService;
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

/**
 * 订单控制器
 *
 * @author system
 * @since 2025-01-24
 */
@Slf4j
@Api(tags = "订单管理")
@RequestMapping(Const.API + "order")
@RequiredArgsConstructor
@RestController
public class OrderController {

    private final IOrderService orderService;

    @ApiOperation("分页查询订单列表")
    @GetMapping("page")
    public PageResult<Order> page(
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Integer status,
            @Validated RequestPage param) {
        return orderService.page(buyerId, sellerId, status, param);
    }

    @ApiOperation("我的买家订单")
    @GetMapping("myBuyOrders")
    public PageResult<Order> myBuyOrders(
            @RequestParam(required = false) Integer status,
            @Validated RequestPage param) {
        Long userId = HttpKit.getUserId();
        return orderService.page(userId, null, status, param);
    }

    @ApiOperation("我的卖家订单")
    @GetMapping("mySellOrders")
    public PageResult<Order> mySellOrders(
            @RequestParam(required = false) Integer status,
            @Validated RequestPage param) {
        Long userId = HttpKit.getUserId();
        return orderService.page(null, userId, status, param);
    }

    @ApiOperation("创建订单")
    @PostMapping("create")
    public R<String> createOrder(@RequestParam Long bookId, @RequestParam Integer quantity) {
        return orderService.createOrder(bookId, quantity);
    }

    @ApiOperation("取消订单")
    @PostMapping("cancel")
    public R<String> cancelOrder(@RequestParam Long orderId) {
        return orderService.cancelOrder(orderId);
    }

    @ApiOperation("支付订单")
    @PostMapping("pay")
    public R<String> payOrder(@RequestParam Long orderId) {
        return orderService.payOrder(orderId);
    }

    @ApiOperation("发货")
    @PostMapping("deliver")
    public R<String> deliverOrder(@RequestParam Long orderId) {
        return orderService.deliverOrder(orderId);
    }

    @ApiOperation("确认收货")
    @PostMapping("confirm")
    public R<String> confirmOrder(@RequestParam Long orderId) {
        return orderService.confirmOrder(orderId);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("detail")
    public R<Order> findById(@RequestParam Long orderId) {
        return orderService.findById(orderId);
    }
}
