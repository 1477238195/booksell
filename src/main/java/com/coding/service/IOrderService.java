package com.coding.service;

import com.coding.entity.Order;
import com.coding.utils.PageResult;
import com.coding.utils.R;
import com.coding.utils.RequestPage;

/**
 * 订单Service接口
 *
 * @author system
 * @since 2025-01-24
 */
public interface IOrderService {

    /**
     * 分页查询订单列表
     */
    PageResult<Order> page(Long buyerId, Long sellerId, Integer status, RequestPage param);

    /**
     * 创建订单
     */
    R<String> createOrder(Long bookId, Integer quantity);

    /**
     * 取消订单
     */
    R<String> cancelOrder(Long orderId);

    /**
     * 支付订单
     */
    R<String> payOrder(Long orderId);

    /**
     * 发货
     */
    R<String> deliverOrder(Long orderId);

    /**
     * 确认收货
     */
    R<String> confirmOrder(Long orderId);

    /**
     * 查询订单详情
     */
    R<Order> findById(Long orderId);
}
