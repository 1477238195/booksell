-- 添加用户余额字段（二手币）
-- 执行方式：mysql -h 123.6.146.2 -P 33306 -u SZJ -pSzj123456 szj < add_balance_field.sql

USE szj;

-- 给 user 表添加 balance 字段（二手币余额）
ALTER TABLE `user` ADD COLUMN `balance` decimal(10,2) DEFAULT 10000.00 COMMENT '二手币余额，默认10000' AFTER `status`;

-- 更新现有用户的余额为10000（如果为NULL）
UPDATE `user` SET `balance` = 10000.00 WHERE `balance` IS NULL;

SELECT '✓ balance 字段添加成功！所有用户初始余额为10000二手币' AS Result;
