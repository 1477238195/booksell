-- 添加 deleted 字段到三个表
-- 执行方式：mysql -h123.6.146.2 -P33306 -uSZJ -pSzj123456 szj < add_deleted_fields.sql

USE szj;

-- 给 order 表添加 deleted 字段
ALTER TABLE `order` ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 给 book_wanted 表添加 deleted 字段
ALTER TABLE `book_wanted` ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

-- 给 message 表添加 deleted 字段
ALTER TABLE `message` ADD COLUMN `deleted` tinyint DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除';

SELECT '✓ deleted 字段添加成功！' AS Result;
