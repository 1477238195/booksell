-- 添加求购表的封面图片字段
ALTER TABLE book_wanted ADD COLUMN IF NOT EXISTS cover_image MEDIUMTEXT NULL COMMENT '封面图片URL或内联 data URI';
