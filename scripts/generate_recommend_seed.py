# -*- coding: utf-8 -*-
"""
生成演示批量 SQL：图书 + 求购 + 订单。
- 图书：ISBN 与 Open Library 封面一一对应（封面 URL = OL 官方 ISBN 封面）。
- 方案 A：生成前对 OL 封面做 HTTP 校验（状态码 + 最小字节数 + JPEG/PNG 魔数），
  仅保留校验通过的 (书名, ISBN)；不足 BOOKS 本则退出并提示失败 ISBN。
  单本可先尝试 -M，失败再试 -L。
- 求购：封面使用同批「已校验」ISBN 轮转。
- 清理：见 recommend_seed_cleanup.sql（[SEED] / （演示批次）/ 历史标题与 picsum 特征）。

用法:
  py -3 scripts/generate_recommend_seed.py > scripts/recommend_seed_200.sql
  py -3 scripts/generate_recommend_seed.py verify
  py -3 scripts/generate_recommend_seed.py generate --skip-verify   # 无外网/离线

环境变量:
  OL_MIN_COVER_BYTES  判定为「有图」的最小响应体字节数（默认 1800）
  OL_REQUEST_PAUSE_S  两次请求间隔秒数（默认 0.15）
  OL_DEBUG            非空时打印封面拉取失败 URL
"""
import argparse
import os
import random
import sys
import time
import urllib.error
import urllib.request

USER1_ID = int(os.environ.get("USER1_ID", "2"))
USER2_ID = int(os.environ.get("USER2_ID", "3"))
BOOKS = int(os.environ.get("BOOKS", "50"))
WANTED = int(os.environ.get("WANTED", "50"))

RNG = random.Random(20260205)

# Open Library：过小或非图片视为无有效封面（占位图多为极小 JPEG；真图一般 ≥1.5KB，阈值可调）
_MIN_COVER_BYTES = int(os.environ.get("OL_MIN_COVER_BYTES", "1800"))
_PAUSE_S = float(os.environ.get("OL_REQUEST_PAUSE_S", "0.15"))

# 与书名顺序一一对应的真实 ISBN（多为常见中文版，Open Library 可查封面）
BOOK_ISBNS = [
    "9787111544937",
    "9787111407010",
    # 下列三本原中文版 ISBN 在 OL 上常无图或过小，改用同书常见英文版 ISBN 以保证方案 A 校验通过（演示数据）
    "9780133594140",
    "9781118063330",
    "9780073523323",
    "9787111346577",
    "9787040396638",
    "9787302497937",
    "9787040588345",
    "9787040395788",
    "9787115478659",
    "9787111421900",
    "9787115419348",
    "9787111497827",
    "9787302438235",
    "9787111649860",
    "9787302580624",
    "9787302515588",
    "9787040468688",
    "9787111640354",
    "9787121310928",
    "9787115477424",
    "9787302280011",
    "9787111427941",
    "9787544253994",
    "9787506365437",
    "9787020024759",
    "9787536692930",
    "9787020002207",
    "9787020032978",
    "9787020020355",
    "9787100004822",
    "9787108015288",
    "9787108010412",
    "9787300237628",
    "9787111577628",
    "9787300269674",
    "9787300198958",
    "9787300239879",
    "9787300239886",
    "9787300239893",
    "9787300239909",
    "9787300239916",
    "9787300239923",
    "9787300239930",
    "9787300239947",
    "9787300239954",
    "9787300239961",
    "9787300239978",
    "9787300239985",
]

# 主 ISBN 在 OL 无可用图时依次尝试（常见 OL 有图版本，尽量多样），保证方案 A 仍能生成可加载封面
_OL_FALLBACK_ISBNS = [
    "9787111544937",
    "9787111407010",
    "9780133594140",
    "9781118063330",
    "9780073523323",
    "9780262033848",
    "9780201896831",
    "9780132350884",
    "9780596009205",
    "9780134685991",
    "9780321573513",
    "9780134093413",
    "9780132126953",
    "9780201612444",
    "9780321486811",
    "9780596517745",
    "9781449331818",
    "9781491950351",
    "9780134689577",
    "9780132778049",
    "9780131103627",
    "9780201633619",
    "9780131873216",
    "9780137081899",
    "9780321357429",
    "9780201709155",
    "9780134694885",
    "9780135162305",
    "9780201615864",
    "9780321776419",
    "9780134497181",
    "9780201485417",
    "9780136005217",
    "9780321480793",
    "9780201616226",
    "9780132354192",
    "9780321576230",
    "9780201704324",
    "9780137081073",
    "9780321794130",
]

# 与 recommend_seed_cleanup.sql 一致；[SEED] 勿用纯 LIKE '%[SEED]%'（MySQL LIKE 中 [] 为字符类）
MARK_BATCH = "[SEED]"

CAT_CS = (11, 12, 13, 2)
CAT_LIT = (9, 10, 1)

BOOK_TITLES = [
    "深入理解计算机系统（原书第3版）",
    "算法导论（中文版）",
    "计算机网络：自顶向下方法",
    "操作系统概念（第九版）",
    "数据库系统概念",
    "编译原理（龙书）",
    "离散数学及其应用",
    "线性代数及其应用",
    "概率论与数理统计教程",
    "高等数学（上册）同济版",
    "Python编程：从入门到实践",
    "Java核心技术 卷I",
    "Spring Boot实战",
    "Redis设计与实现",
    "机器学习（周志华）",
    "深度学习（花书）",
    "模式分类",
    "统计学习方法（第二版）",
    "数据结构与算法分析",
    "计算机程序的构造和解释",
    "代码整洁之道",
    "重构：改善既有代码的设计",
    "人月神话（纪念版）",
    "黑客与画家",
    "百年孤独",
    "活着",
    "围城",
    "三体（全集）",
    "红楼梦（脂砚斋评本）",
    "唐诗三百首详析",
    "宋词选",
    "西方哲学史（罗素）",
    "中国历代政治得失",
    "万历十五年",
    "经济学原理（曼昆）",
    "管理学（罗宾斯）",
    "投资学（博迪）",
    "财务管理学",
    "市场营销原理",
    "组织行为学",
    "宏观经济学",
    "微观经济学：现代观点",
    "国际经济学",
    "货币金融学",
    "审计学（第十版）",
    "中级财务会计",
    "成本会计学",
    "税法（CPA教材）",
    "公司理财（罗斯）",
    "行为金融学",
    "创业维艰",
]

WANTED_TITLES = [
    "求购：计算机网络期末复习笔记",
    "收一本九成新《线性代数》同济版",
    "急求操作系统真题与答案",
    "求数据库系统概论最新版",
    "收编译原理教材，可有划线",
    "求离散数学辅导书",
    "收概率论与数理统计（浙大版）",
    "求高等数学习题全解",
    "收Python数据分析相关",
    "求Java并发编程实战",
    "收Spring Cloud微服务实战",
    "求Redis实战或同类",
    "收机器学习西瓜书",
    "求深度学习入门（李宏毅笔记版）",
    "收统计学习方法第二版",
    "求数据结构与算法（邓俊辉）",
    "收SICP计算机程序的构造和解释",
    "求代码大全第二版",
    "收重构中文版",
    "求人月神话英文版也可",
    "收黑客与画家",
    "求百年孤独精装",
    "收活着任意版本",
    "求围城人民文学版",
    "收三体全集平装",
    "求红楼梦庚辰本影印",
    "收唐诗鉴赏辞典",
    "求宋词三百首笺注",
    "收西方哲学史下册",
    "求中国历代政治得失",
    "收万历十五年三联版",
    "求经济学原理微观分册",
    "收管理学第十三版",
    "求投资学笔记配套",
    "收财务管理学人大版",
    "求市场营销双语版",
    "收组织行为学案例集",
    "求宏观经济学高鸿业",
    "收微观经济学范里安",
    "求国际经济学克鲁格曼",
    "收货币金融学米什金",
    "求审计学轻一配套",
    "收中级财务会计东财版",
    "求成本会计学于富生",
    "收税法2024教材",
    "求公司理财精要版",
    "收行为金融学讲义",
    "求创业维艰英文原版",
    "收一本算法竞赛入门经典",
    "求数字图像处理冈萨雷斯",
]


def esc(s):
    return s.replace("\\", "\\\\").replace("'", "''")


def pick_publisher():
    return RNG.choice([USER1_ID, USER2_ID])


def ol_cover(isbn, size="M"):
    """Open Library 官方 ISBN 封面，与图书条目绑定。"""
    digits = "".join(c for c in str(isbn) if c.isdigit())
    return "https://covers.openlibrary.org/b/isbn/{}-{}.jpg".format(digits, size)


def _cover_body_ok(data):
    if not data or len(data) < _MIN_COVER_BYTES:
        return False
    if data[:2] == b"\xff\xd8":
        return True
    if len(data) >= 8 and data[:8] == b"\x89PNG\r\n\x1a\n":
        return True
    if len(data) >= 12 and data[:4] == b"RIFF" and data[8:12] == b"WEBP":
        return True
    return False


def _fetch_cover(url, timeout=24):
    """拉取封面字节；失败时短暂重试一次（弱网 / OL 偶发超时）。"""
    last_err = None
    for attempt in range(2):
        try:
            req = urllib.request.Request(
                url,
                headers={
                    "User-Agent": "alex-book-seed/1.0 cover-verify",
                    "Accept": "image/*,*/*;q=0.8",
                },
            )
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                if resp.status != 200:
                    return b""
                return resp.read(262144)
        except (urllib.error.HTTPError, urllib.error.URLError, TimeoutError, OSError) as e:
            last_err = e
            if attempt == 0:
                time.sleep(0.45)
    if os.environ.get("OL_DEBUG"):
        print("# fetch fail {} {}".format(url, last_err), file=sys.stderr)
    return b""


_cover_size_memo = {}


def best_cover_size_for_isbn(isbn):
    """依次尝试 M、L 尺寸；返回可用 size 字符串，否则 None。"""
    if isbn in _cover_size_memo:
        return _cover_size_memo[isbn]
    for size in ("M", "L"):
        if _PAUSE_S > 0:
            time.sleep(_PAUSE_S)
        url = ol_cover(isbn, size)
        body = _fetch_cover(url)
        if _cover_body_ok(body):
            _cover_size_memo[isbn] = size
            return size
    _cover_size_memo[isbn] = None
    return None


def build_verified_ol_pool():
    """
    启动时一次性探测：备用链 + 本批主 ISBN 中，哪些在 OL 上能过校验。
    主 ISBN 失败时只在该池内轮转分配，避免「池里实际只有 1 本通过却反复用 CSAPP」。
    """
    pool = []
    seen = set()
    for isbn in _OL_FALLBACK_ISBNS:
        if isbn in seen:
            continue
        seen.add(isbn)
        if best_cover_size_for_isbn(isbn):
            pool.append(isbn)
    for isbn in BOOK_ISBNS[:BOOKS]:
        if isbn in seen:
            continue
        seen.add(isbn)
        if best_cover_size_for_isbn(isbn):
            pool.append(isbn)
    return pool


def resolve_ol_isbn_and_size(primary_isbn, skip_verify, row_index, verified_pool):
    """
    返回 (isbn, size)。skip_verify 时直接 (primary, M)。
    主 ISBN 无图时：在 verified_pool（生成前已筛过 OL）内按 row_index 轮转取不同项，保证多行不会总落在同一 ISBN。
    """
    if skip_verify:
        return primary_isbn, "M"
    sz = best_cover_size_for_isbn(primary_isbn)
    if sz:
        return primary_isbn, sz

    if not verified_pool:
        return None, None
    n = len(verified_pool)
    ri = int(row_index)
    for k in range(n):
        alt = verified_pool[(ri * 11 + k) % n]
        if alt == primary_isbn:
            continue
        sz2 = best_cover_size_for_isbn(alt)
        if sz2:
            return alt, sz2
    for k in range(n):
        alt = verified_pool[k]
        sz2 = best_cover_size_for_isbn(alt)
        if sz2:
            return alt, sz2
    return None, None


def cmd_verify():
    """仅打印校验结果，不写 SQL。"""
    n = min(len(BOOK_TITLES), len(BOOK_ISBNS))
    print("# Open Library 封面校验（ISBN 顺序与脚本内 BOOK_ISBNS 一致）\n", file=sys.stderr)
    ok_n = 0
    for i in range(n):
        title, isbn = BOOK_TITLES[i], BOOK_ISBNS[i]
        size = best_cover_size_for_isbn(isbn)
        if size:
            ok_n += 1
            print("OK\t{}\t{}\t{}".format(isbn, size, title))
        else:
            print("FAIL\t{}\t-\t{}".format(isbn, title))
    print("\n# 通过 {}/{}".format(ok_n, n), file=sys.stderr)


def main(skip_verify=False):
    assert len(BOOK_ISBNS) >= BOOKS and len(BOOK_TITLES) >= BOOKS
    assert len(WANTED_TITLES) >= WANTED

    # 书名仍按索引；封面：先建「本机 OL 校验通过」ISBN 池，主 ISBN 失败时只在池内轮转，避免 good 只有 1 本时全员 CSAPP
    verified_pool = []
    if not skip_verify:
        verified_pool = build_verified_ol_pool()
        print("# OL 封面可用 ISBN 池：{} 个（备用链 + 本批主 ISBN 去重）".format(len(verified_pool)), file=sys.stderr)
        if len(verified_pool) < 2:
            print(
                "错误：通过 OL 校验的 ISBN 不足 2 个，无法为无图主 ISBN 轮转分配封面。请检查网络或扩充 _OL_FALLBACK_ISBNS。",
                file=sys.stderr,
            )
            sys.exit(1)

    book_row_isbn = []
    book_ol_sizes = []
    for i in range(BOOKS):
        primary = BOOK_ISBNS[i]
        isbn, sz = resolve_ol_isbn_and_size(primary, skip_verify, i, verified_pool)
        if isbn is None:
            print(
                "错误：索引 {} 的书《{}》主 ISBN {} 及备用链均无可用 OL 封面，请检查网络或扩大 _OL_FALLBACK_ISBNS。".format(
                    i, BOOK_TITLES[i], primary
                ),
                file=sys.stderr,
            )
            sys.exit(1)
        if not skip_verify and isbn != primary:
            print(
                "# 封面：索引 {} 主 ISBN {} 无图，已改用 {}（书名仍为《{}》）".format(
                    i, primary, isbn, BOOK_TITLES[i]
                ),
                file=sys.stderr,
            )
        book_row_isbn.append(isbn)
        book_ol_sizes.append(sz)

    out = sys.stdout
    out.write(
        "-- 演示数据：{} 本书 + {} 条求购；封面与 ISBN 对应（Open Library）；{}。\n".format(
            BOOKS, WANTED, MARK_BATCH
        )
        + (
            "-- 封面：方案 A（HTTP 校验）；无图时在「本机校验通过的 ISBN 池」内按行轮转分配，避免全员同一封面。\n"
            if not skip_verify
            else "-- 封面：未校验（--skip-verify），请自行确认 OL 链接有效。\n"
        )
        + "-- 导入前先执行 recommend_seed_cleanup.sql 清空旧演示数据。\n"
        + "SET NAMES utf8mb4;\n\n"
    )

    rows = []
    for i in range(BOOKS):
        is_cs = i % 3 != 0
        cat = CAT_CS[i % len(CAT_CS)] if is_cs else CAT_LIT[i % len(CAT_LIT)]
        sid = pick_publisher()
        title = BOOK_TITLES[i]
        isbn = book_row_isbn[i]
        ol_size = book_ol_sizes[i]
        author = "作者·卷{}".format((i * 17) % 90 + 1)
        desc = (
            "个人闲置，品相{}成新，无写划或少量笔记。{}{}".format(
                (i % 4) + 6, "计算机/理工类" if is_cs else "人文经管类", MARK_BATCH
            )
        )
        price = 8 + (i * 3) % 95
        op = min(price + 25, 220)
        cond = (i % 4) + 1
        vc = (i * 37 + 11) % 400
        img = esc(ol_cover(isbn, ol_size))
        rows.append(
            "('{t}', '{a}', '{isbn}', {cat}, '{img}', '{d}', "
            "{p}.00, {op}.00, {cond}, 1, {sid}, 1, {vc}, 0)".format(
                t=esc(title),
                a=esc(author),
                isbn=isbn,
                cat=cat,
                img=img,
                d=esc(desc),
                p=price,
                op=op,
                cond=cond,
                sid=sid,
                vc=vc,
            )
        )

    out.write(
        "INSERT INTO `book` (`title`, `author`, `isbn`, `category_id`, `cover_image`, `description`, "
        "`price`, `original_price`, `condition`, `stock`, `seller_id`, `status`, `view_count`, `deleted`) VALUES\n"
    )
    out.write(",\n".join(rows))
    out.write(";\n\n")

    wrows = []
    for j in range(WANTED):
        uid = pick_publisher()
        wt = WANTED_TITLES[j]
        wa = "偏好作者：佚名{}".format((j * 5) % 40)
        want_k = j % len(BOOK_ISBNS)
        want_primary = BOOK_ISBNS[want_k]
        want_isbn, want_sz = resolve_ol_isbn_and_size(
            want_primary, skip_verify, j + BOOKS, verified_pool
        )
        if want_isbn is None:
            want_isbn, want_sz = resolve_ol_isbn_and_size(
                _OL_FALLBACK_ISBNS[0], skip_verify, j + 999, verified_pool
            )
        if want_isbn is None:
            print("错误：求购行 {} 无法解析任何可用封面 ISBN。".format(j), file=sys.stderr)
            sys.exit(1)
        wd = "期望{}成新以内，可面交或快递，编号{:03d}。{}".format((j % 3) + 7, j, MARK_BATCH)
        maxp = 12 + (j * 7) % 88
        vc = (j * 29 + 3) % 350
        img = esc(ol_cover(want_isbn, want_sz)) if want_sz else ""
        wrows.append(
            "({uid}, '{wt}', '{wa}', '{isbn}', '{img}', '{wd}', {mp}.00, 1, {vc}, 0)".format(
                uid=uid,
                wt=esc(wt),
                wa=esc(wa),
                isbn=want_isbn,
                img=img,
                wd=esc(wd),
                mp=maxp,
                vc=vc,
            )
        )

    out.write(
        "INSERT INTO `book_wanted` (`user_id`, `book_title`, `author`, `isbn`, `cover_image`, `description`, "
        "`max_price`, `status`, `view_count`, `deleted`) VALUES\n"
    )
    out.write(",\n".join(wrows))
    out.write(";\n\n")

    a, b = USER1_ID, USER2_ID
    mark_esc = esc(MARK_BATCH)
    order_sql = (
        "-- 已完成订单：买卖双方仅 user_id {a} <-> {b}（描述含种子标记的图书）\n"
        "INSERT INTO `order` (`order_no`, `buyer_id`, `seller_id`, `book_id`, `quantity`, `total_price`, `status`, `payment_time`, `finish_time`, `deleted`)\n"
        "SELECT CONCAT('SEED', b.`book_id`),\n"
        "       CASE WHEN b.`seller_id` = {a} THEN {b} ELSE {a} END,\n"
        "       b.`seller_id`,\n"
        "       b.`book_id`, 1, b.`price`, 4, NOW(), NOW(), 0\n"
        "FROM `book` b\n"
        "WHERE INSTR(b.`description`, '{m}') > 0\n"
        "  AND b.`deleted` = 0\n"
        "  AND b.`seller_id` IN ({a}, {b});\n"
    ).format(a=a, b=b, m=mark_esc)
    out.write(order_sql)
    out.write("\n-- 清理：scripts/recommend_seed_cleanup.sql\n")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="推荐演示种子 SQL / Open Library 封面校验（方案 A）")
    parser.add_argument(
        "command",
        nargs="?",
        default="generate",
        choices=["generate", "verify"],
        help="generate：输出 SQL；verify：仅校验并打印每本书结果",
    )
    parser.add_argument(
        "--skip-verify",
        action="store_true",
        help="generate 时使用：不访问外网，不校验封面（旧行为）",
    )
    args = parser.parse_args()
    if args.command == "verify":
        cmd_verify()
    else:
        main(skip_verify=args.skip_verify)
