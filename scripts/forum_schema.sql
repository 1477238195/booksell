-- 论坛模块表结构（MySQL 8+；H2 使用 JPA ddl-auto 或按方言调整）
-- 与 com.coding.entity 中 Forum* 实体对应

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `forum_board` (
  `board_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `slug` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `sort_order` int NOT NULL DEFAULT 0,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0-隐藏 1-显示',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`board_id`),
  UNIQUE KEY `uk_forum_board_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛版块';

CREATE TABLE IF NOT EXISTS `forum_topic` (
  `topic_id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` mediumtext NOT NULL,
  `book_id` bigint DEFAULT NULL,
  `view_count` int NOT NULL DEFAULT 0,
  `reply_count` int NOT NULL DEFAULT 0,
  `last_reply_at` datetime DEFAULT NULL,
  `last_reply_user_id` bigint DEFAULT NULL,
  `pinned` tinyint NOT NULL DEFAULT 0,
  `locked` tinyint NOT NULL DEFAULT 0,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`topic_id`),
  KEY `idx_forum_topic_board` (`board_id`,`deleted`,`last_reply_at`),
  KEY `idx_forum_topic_book` (`book_id`),
  KEY `idx_forum_topic_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛主题';

CREATE TABLE IF NOT EXISTS `forum_reply` (
  `reply_id` bigint NOT NULL AUTO_INCREMENT,
  `topic_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `parent_reply_id` bigint DEFAULT NULL,
  `content` mediumtext NOT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`reply_id`),
  KEY `idx_forum_reply_topic` (`topic_id`,`deleted`,`create_time`),
  KEY `idx_forum_reply_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛回复';

INSERT INTO `forum_board` (`name`, `slug`, `description`, `sort_order`, `status`) VALUES
('综合讨论', 'general', '闲聊与站内话题', 10, 1),
('书评读后感', 'review', '分享阅读心得', 20, 1),
('求书换书', 'seek', '求书、换书、交流', 30, 1),
('交易相关', 'trade', '交易流程与经验', 40, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
