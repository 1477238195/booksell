-- 清理种子脚本导入的数据（与 recommend_seed_200_fixed.sql 等对齐）
-- MySQL 中 LIKE '%[SEED]%' 不可靠（[] 为通配符），请用 INSTR / LOCATE。
-- 若库中无 book_comment 表，请删除下方「书籍评论」整段 DELETE。

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- 订单：种子单号 SEED… 或备注含「种子订单」
DELETE FROM `order`
WHERE `order_no` LIKE 'SEED%'
   OR `remark` LIKE '%种子订单%';

-- 仍挂在种子书上的订单（防止单号/备注被改过导致上一句删不干净）
DELETE o FROM `order` o
INNER JOIN `book` b ON o.`book_id` = b.`book_id`
WHERE INSTR(b.`description`, '[SEED]') > 0
   OR INSTR(b.`description`, '种子批次') > 0
   OR b.`description` LIKE '%（演示批次）%'
   OR b.`title` LIKE '【推荐种子】%'
   OR b.`title` LIKE '【推荐测试】书%'
   OR b.`cover_image` LIKE 'https://picsum.photos/seed/bkseed%'
   OR (
         b.`cover_image` LIKE 'https://covers.openlibrary.org%'
     AND b.`description` LIKE '%（演示批次）%'
   );

-- 求购
DELETE FROM `book_wanted`
WHERE INSTR(`description`, '[SEED]') > 0
   OR INSTR(`description`, '种子批次') > 0
   OR `description` LIKE '%（演示批次）%'
   OR `book_title` LIKE '【推荐种子求购】%'
   OR `book_title` LIKE '【推荐测试求购】%'
   OR `cover_image` LIKE 'https://picsum.photos/seed/wtseed%';

-- 书籍评论（有 fk_comment_book 时需先于 book 删除）
DELETE bc FROM `book_comment` bc
INNER JOIN `book` b ON bc.`book_id` = b.`book_id`
WHERE INSTR(b.`description`, '[SEED]') > 0
   OR INSTR(b.`description`, '种子批次') > 0
   OR b.`description` LIKE '%（演示批次）%'
   OR b.`title` LIKE '【推荐种子】%'
   OR b.`title` LIKE '【推荐测试】书%'
   OR b.`cover_image` LIKE 'https://picsum.photos/seed/bkseed%'
   OR (
         b.`cover_image` LIKE 'https://covers.openlibrary.org%'
     AND b.`description` LIKE '%（演示批次）%'
   );

-- 书籍
DELETE FROM `book`
WHERE INSTR(`description`, '[SEED]') > 0
   OR INSTR(`description`, '种子批次') > 0
   OR `description` LIKE '%（演示批次）%'
   OR `title` LIKE '【推荐种子】%'
   OR `title` LIKE '【推荐测试】书%'
   OR `cover_image` LIKE 'https://picsum.photos/seed/bkseed%'
   OR (
         `cover_image` LIKE 'https://covers.openlibrary.org%'
     AND `description` LIKE '%（演示批次）%'
   );

COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Cleanup done.' AS status;
SELECT COUNT(*) AS remaining_books FROM `book`;
SELECT COUNT(*) AS remaining_wanted FROM `book_wanted`;
SELECT COUNT(*) AS remaining_orders FROM `order`;
