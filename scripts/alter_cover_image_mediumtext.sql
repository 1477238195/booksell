-- 解决 recommend_seed_200_fixed.sql 等种子脚本报错：
-- Error 1406: Data too long for column 'cover_image' at row 1
-- 内联 SVG（data:image/svg+xml,...）远超 varchar(500)，需扩大字段。

SET NAMES utf8mb4;

ALTER TABLE `book`
  MODIFY COLUMN `cover_image` MEDIUMTEXT NULL COMMENT '封面图片URL或内联 data URI';

-- 求购表：若尚无 cover_image 列，请先执行 src/main/resources/schema-update.sql（或 add_column.sql），再执行下面一句。
ALTER TABLE `book_wanted`
  MODIFY COLUMN `cover_image` MEDIUMTEXT NULL COMMENT '封面图片URL或内联 data URI';
