package com.coding.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author system
 * @since 2025-01-24
 */
@Data
@Accessors(chain = true)
@Table(name = "`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    /**
     * 订单编号
     */
    @Column(name = "order_no")
    private String orderNo;

    /**
     * 买家ID
     */
    @Column(name = "buyer_id")
    private Long buyerId;

    /**
     * 卖家ID
     */
    @Column(name = "seller_id")
    private Long sellerId;

    /**
     * 书籍ID
     */
    @Column(name = "book_id")
    private Long bookId;

    /**
     * 数量
     */
    @Column(name = "quantity")
    private Integer quantity;

    /**
     * 订单总价
     */
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    /**
     * 订单状态：1-待支付，2-待发货，3-待收货，4-已完成，5-已取消
     */
    @Column(name = "status")
    private Integer status;

    /**
     * 支付时间
     */
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    /**
     * 发货时间
     */
    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    /**
     * 完成时间
     */
    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @Column(name = "deleted")
    private Integer deleted;

    // 关联查询字段
    @Transient
    private String bookTitle;

    @Transient
    private String bookCoverImage;

    @Transient
    private String buyerName;

    @Transient
    private String sellerName;
}
