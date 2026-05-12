-- 检查书籍数据
-- 执行方式：mysql -h 123.6.146.2 -P 33306 -u SZJ -pSzj123456 szj < check_books.sql

USE szj;

-- 1. 查看最近发布的10本书籍及其状态
SELECT 
    book_id AS '书籍ID',
    title AS '书名',
    status AS '状态',
    CASE 
        WHEN status = 0 THEN '已下架'
        WHEN status = 1 THEN '在售'
        WHEN status = 2 THEN '已售出'
        ELSE '未知'
    END AS '状态说明',
    deleted AS '是否删除',
    seller_id AS '卖家ID',
    DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') AS '创建时间'
FROM book
ORDER BY create_time DESC
LIMIT 10;

-- 2. 统计各状态的书籍数量（未删除的）
SELECT 
    status AS '状态值',
    CASE 
        WHEN status = 0 THEN '已下架'
        WHEN status = 1 THEN '在售'
        WHEN status = 2 THEN '已售出'
        ELSE '未知'
    END AS '状态说明',
    COUNT(*) AS '数量'
FROM book
WHERE deleted = 0
GROUP BY status
ORDER BY status;

-- 3. 查看所有在售且未删除的书籍
SELECT 
    book_id,
    title,
    author,
    price,
    status,
    seller_id,
    DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') AS create_time
FROM book
WHERE deleted = 0 AND status = 1
ORDER BY create_time DESC;

-- 4. 检查是否有status为NULL的书籍
SELECT COUNT(*) AS 'status为NULL的数量'
FROM book
WHERE status IS NULL AND deleted = 0;
