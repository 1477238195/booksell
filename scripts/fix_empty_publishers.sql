-- 修复「发布人为空」：接口无昵称时前端会显示「匿名卖家 / 书友」
--
-- 当前约定（与你库中 user 表一致）：
--   user1 -> user_id = 2
--   user2 -> user_id = 3
-- 若你的环境自增不同，请先执行下方「校验」再改常量。
--
-- 规则：book.seller_id 为 NULL 或 0 -> 2（user1）
--       book_wanted.user_id 为 NULL 或 0 -> 3（user2）
-- 执行前建议备份。

SET NAMES utf8mb4;

-- ========== 校验（建议执行一次）==========
-- SELECT user_id, username FROM user WHERE user_id IN (2, 3) AND deleted = 0;
-- 期望：2=user1，3=user2

START TRANSACTION;

UPDATE `book`
SET `seller_id` = 2
WHERE `deleted` = 0
  AND (`seller_id` IS NULL OR `seller_id` = 0);

UPDATE `book_wanted`
SET `user_id` = 3
WHERE `deleted` = 0
  AND (`user_id` IS NULL OR `user_id` = 0);

COMMIT;
