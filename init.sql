-- 二手书交易平台数据库初始化脚本
-- 数据库: demo1223

-- 如果表存在则删除
DROP TABLE IF EXISTS `message`;
DROP TABLE IF EXISTS `book_wanted`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `book_category`;
DROP TABLE IF EXISTS `user`;

-- 创建用户表
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(200) NOT NULL COMMENT '密码（加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `role` int DEFAULT 0 COMMENT '角色：0-普通用户，1-管理员',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `status` int DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
  `balance` decimal(12,2) NOT NULL DEFAULT '10000.00' COMMENT '二手币余额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建书籍分类表
CREATE TABLE `book_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint DEFAULT 0 COMMENT '父分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `icon` varchar(200) DEFAULT NULL COMMENT '图标',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍分类表';

-- 创建书籍表
CREATE TABLE `book` (
  `book_id` bigint NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
  `title` varchar(200) NOT NULL COMMENT '书名',
  `author` varchar(100) DEFAULT NULL COMMENT '作者',
  `isbn` varchar(50) DEFAULT NULL COMMENT 'ISBN编号',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `cover_image` mediumtext COMMENT '封面图片URL或内联 data URI（可存长 SVG）',
  `description` text COMMENT '书籍描述',
  `price` decimal(10,2) DEFAULT NULL COMMENT '价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `condition` tinyint DEFAULT 1 COMMENT '新旧程度：1-全新，2-几乎全新，3-轻微使用痕迹，4-明显使用痕迹',
  `stock` int DEFAULT 1 COMMENT '库存数量',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-已下架，1-在售，2-已售出',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`book_id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍表';

-- 创建订单表
CREATE TABLE `order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `book_id` bigint NOT NULL COMMENT '书籍ID',
  `quantity` int DEFAULT 1 COMMENT '数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '订单总价',
  `status` tinyint DEFAULT 1 COMMENT '订单状态：1-待支付，2-待发货，3-待收货，4-已完成，5-已取消',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_buyer` (`buyer_id`),
  KEY `idx_seller` (`seller_id`),
  KEY `idx_book` (`book_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 创建求购信息表
CREATE TABLE `book_wanted` (
  `wanted_id` bigint NOT NULL AUTO_INCREMENT COMMENT '求购ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `book_title` varchar(200) NOT NULL COMMENT '求购书名',
  `author` varchar(100) DEFAULT NULL COMMENT '作者',
  `isbn` varchar(50) DEFAULT NULL COMMENT 'ISBN',
  `description` text COMMENT '求购说明',
  `max_price` decimal(10,2) DEFAULT NULL COMMENT '期望最高价格',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-已关闭，1-求购中，2-已找到',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`wanted_id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='求购信息表';

-- 创建站内消息表
CREATE TABLE `message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `type` tinyint DEFAULT 1 COMMENT '消息类型：1-文本，2-图片',
  `is_read` tinyint DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`message_id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_receiver_read` (`receiver_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息表';

-- 插入书籍分类初始数据
INSERT INTO `book_category` (`category_id`, `parent_id`, `name`, `sort_order`, `status`) VALUES
(1, 0, '文学', 1, 1),
(2, 0, '计算机', 2, 1),
(3, 0, '经济管理', 3, 1),
(4, 0, '外语学习', 4, 1),
(5, 0, '教材教辅', 5, 1),
(6, 0, '艺术设计', 6, 1),
(7, 0, '历史传记', 7, 1),
(8, 0, '科学技术', 8, 1),
(9, 1, '小说', 1, 1),
(10, 1, '诗歌散文', 2, 1),
(11, 2, '编程语言', 1, 1),
(12, 2, '数据库', 2, 1),
(13, 2, '人工智能', 3, 1),
(14, 3, '经济学', 1, 1),
(15, 3, '管理学', 2, 1);

-- 插入测试管理员账号
-- 用户名: admin  密码: admin123 (BCrypt，与 PasswordUtil / Spring 校验一致)
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `deleted`) VALUES
('admin', '$2b$10$zn3jj96tqs0lCKLT17HuvOElR.B5QmaGhP9Yu3CUHkz2LV4fTjq66', '系统管理员', 1, 1, 0);

-- 插入测试普通用户
-- 用户名: user1  密码: user123
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`, `deleted`) VALUES
('user1', '$2b$10$GbxXhYpHSoBwXfmFLASlJeahqe0mZzzH46TDfOXEZv.7rML.RbQnm', '张三', 0, 1, 0);

COMMIT;
