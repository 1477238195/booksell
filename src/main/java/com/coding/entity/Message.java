package com.coding.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 站内消息实体类
 *
 * @author system
 * @since 2025-01-24
 */
@Data
@Accessors(chain = true)
@Table(name = "message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageId;

    /**
     * 发送者ID
     */
    @Column(name = "sender_id")
    private Long senderId;

    /**
     * 接收者ID
     */
    @Column(name = "receiver_id")
    private Long receiverId;

    /**
     * 消息内容
     */
    @Column(name = "content")
    private String content;

    /**
     * 消息类型：1-文本，2-图片
     */
    @Column(name = "type")
    private Integer type;

    /**
     * 是否已读：0-未读，1-已读
     */
    @Column(name = "is_read")
    private Integer isRead;

    /**
     * 发送时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 逻辑删除
     */
    @Column(name = "deleted")
    private Integer deleted;

    // 关联查询字段
    @Transient
    private String senderName;

    @Transient
    private String receiverName;

    @Transient
    private String senderAvatar;

    @Transient
    private String receiverAvatar;
}
