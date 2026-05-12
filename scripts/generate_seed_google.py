#!/usr/bin/env python3
"""
生成二手书交易平台种子 SQL（输出 recommend_seed_google.sql）

封面来源：Google Books API（volumes?q=isbn:...），从 volumeInfo.imageLinks 选取尽可能大的缩略图 URL，
并可选 GET 校验 JPEG/PNG/WebP 魔数 + 最小体积（与 generate_seed.py 一致）。

使用前请阅读并遵守 Google Books API / Books 内容使用条款：
  https://developers.google.com/books/terms

环境变量（必填）：
  GOOGLE_BOOKS_API_KEY   — Google Cloud 项目中的 Books API 密钥

可选：
  SEED_GOOGLE_MAX_BOOKS       默认 25：最多导出「Google 有可用封面」的前 N 本书
  SEED_GOOGLE_REQUEST_PAUSE_S 默认 0.12：每次调用 Books API 后的休眠（秒）
  SEED_GOOGLE_IMAGE_PAUSE_S   默认 0.04：下载封面校验前的休眠（秒）
  OL_MIN_COVER_BYTES            与 generate_seed 相同，封面最小字节数（默认 800）
  SEED_GOOGLE_SKIP_IMAGE_CHECK 设为 1 则跳过 GET 封面校验（仅用 API 返回的 URL）

运行（Windows PowerShell 示例）：
  $env:GOOGLE_BOOKS_API_KEY="你的密钥"
  python scripts/generate_seed_google.py
"""

from __future__ import annotations

import json
import os
import random
import sys
import time
import urllib.error
import urllib.parse
import urllib.request

_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if _SCRIPT_DIR not in sys.path:
    sys.path.insert(0, _SCRIPT_DIR)

import generate_seed as base

OUTPUT = os.path.join(_SCRIPT_DIR, "recommend_seed_google.sql")

_GOOGLE_VOLUMES = "https://www.googleapis.com/books/v1/volumes"
_IMAGE_LINK_KEYS = (
    "extraLarge",
    "large",
    "medium",
    "small",
    "thumbnail",
    "smallThumbnail",
)

_MIN_COVER_BYTES = int(os.environ.get("OL_MIN_COVER_BYTES", "800"))
_PAUSE_API = float(os.environ.get("SEED_GOOGLE_REQUEST_PAUSE_S", "0.12"))
_PAUSE_IMG = float(os.environ.get("SEED_GOOGLE_IMAGE_PAUSE_S", "0.04"))
_SKIP_IMG = os.environ.get("SEED_GOOGLE_SKIP_IMAGE_CHECK", "").strip() in ("1", "true", "yes")


def escape_sql(s: str) -> str:
    return base.escape_sql(s)


def _cover_body_ok(data: bytes) -> bool:
    return base._cover_body_ok(data)


def _fetch_cover_bytes(url: str, timeout: int = 12) -> bytes:
    try:
        req = urllib.request.Request(
            url,
            headers={
                "User-Agent": "alex-seed-google-books/1.0 (+https://developers.google.com/books)",
                "Accept": "image/*,*/*;q=0.8",
            },
        )
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status != 200:
                return b""
            return resp.read(262144)
    except Exception:
        return b""


def _https_url(url: str) -> str:
    if not url:
        return ""
    u = url.strip()
    if u.startswith("//"):
        return "https:" + u
    if u.startswith("http://"):
        return "https://" + u[len("http://") :]
    return u


def _fetch_volumes_json(isbn_digits: str, api_key: str, timeout: int = 15) -> dict | None:
    q = f"isbn:{isbn_digits}"
    params = urllib.parse.urlencode({"q": q, "key": api_key, "maxResults": "1"})
    url = f"{_GOOGLE_VOLUMES}?{params}"
    try:
        req = urllib.request.Request(
            url,
            headers={
                "User-Agent": "alex-seed-google-books/1.0 (+https://developers.google.com/books)",
                "Accept": "application/json",
            },
        )
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status != 200:
                return None
            raw = resp.read()
        return json.loads(raw.decode("utf-8"))
    except urllib.error.HTTPError as e:
        try:
            body = e.read().decode("utf-8", errors="replace")[:500]
        except Exception:
            body = ""
        print(f"  HTTP {e.code} for ISBN {isbn_digits}: {body}", file=sys.stderr)
        return None
    except Exception as ex:
        print(f"  请求失败 ISBN {isbn_digits}: {ex}", file=sys.stderr)
        return None


def _pick_image_url_from_volume(volume: dict) -> str:
    vi = volume.get("volumeInfo") or {}
    links = vi.get("imageLinks") or {}
    if not isinstance(links, dict):
        return ""
    for key in _IMAGE_LINK_KEYS:
        u = links.get(key)
        if isinstance(u, str) and u.strip():
            return _https_url(u)
    return ""


def _resolve_google_cover(isbn: str, api_key: str) -> str | None:
    digits = "".join(c for c in str(isbn) if c.isdigit())
    if not digits:
        return None
    if _PAUSE_API > 0:
        time.sleep(_PAUSE_API)
    data = _fetch_volumes_json(digits, api_key)
    if not data:
        return None
    items = data.get("items")
    if not isinstance(items, list) or not items:
        return None
    url = _pick_image_url_from_volume(items[0])
    if not url:
        return None
    if _SKIP_IMG:
        return url
    if _PAUSE_IMG > 0:
        time.sleep(_PAUSE_IMG)
    body = _fetch_cover_bytes(url)
    if _cover_body_ok(body):
        return url
    return None


def generate() -> None:
    api_key = (os.environ.get("GOOGLE_BOOKS_API_KEY") or "").strip()
    if not api_key:
        print(
            "错误：请设置环境变量 GOOGLE_BOOKS_API_KEY 后重试。\n"
            "  在 Google Cloud Console 启用 Books API，并创建 API 密钥。\n"
            "  参阅：https://developers.google.com/books/docs/v1/using",
            file=sys.stderr,
        )
        sys.exit(2)

    max_books = int(os.environ.get("SEED_GOOGLE_MAX_BOOKS", "25"))
    sellers = [2, 3, 4, 5, 6]
    conditions = [1, 2, 2, 3, 3, 3, 4]
    statuses = [1] * 8 + [0, 2]
    cover_cache: dict[str, str] = {}

    book_entries: list[tuple[str, str, str, int, str]] = []
    for title, author, isbn, cat_id in base.BOOK_SEEDS:
        if len(book_entries) >= max_books:
            break
        digits = "".join(c for c in str(isbn) if c.isdigit())
        if digits in cover_cache:
            cu = cover_cache[digits]
        else:
            cu = _resolve_google_cover(isbn, api_key)
            if cu:
                cover_cache[digits] = cu
        if not cu:
            continue
        book_entries.append((title, author, isbn, cat_id, cu))

    if len(book_entries) < max_books:
        print(
            f"警告：Google Books 有可用封面的书仅 {len(book_entries)} 本（目标 {max_books}）。"
            "可调高 SEED_GOOGLE_MAX_BOOKS 或检查 ISBN / 网络 / 配额。",
            file=sys.stderr,
        )

    if not book_entries:
        print("错误：没有可导出的图书记录（请检查 API Key、Books API 是否已启用、网络）。", file=sys.stderr)
        sys.exit(1)

    allowed_isbn = {b[2] for b in book_entries}

    lines: list[str] = []
    lines.append(
        "-- 二手书交易平台种子数据：封面来自 Google Books API（imageLinks，经 GET 校验或跳过校验）。"
    )
    lines.append("-- Generated by scripts/generate_seed_google.py")
    lines.append("-- 使用须遵守 https://developers.google.com/books/terms")
    lines.append(f"-- Books: {len(book_entries)}, Wanted rows: (见下方)")
    lines.append("SET NAMES utf8mb4;\n")
    lines.append("START TRANSACTION;\n")

    lines.append(
        "INSERT INTO `book` (`title`, `author`, `isbn`, `category_id`, "
        "`cover_image`, `description`, `price`, `original_price`, "
        "`condition`, `stock`, `seller_id`, `status`, `view_count`, `deleted`) VALUES"
    )

    book_rows = []
    for i, (title, author, isbn, cat_id, cover) in enumerate(book_entries):
        price = round(random.uniform(5, 55), 2)
        orig = round(price * random.uniform(1.3, 3.5), 2)
        cond = random.choice(conditions)
        stock = random.choices([1, 1, 1, 1, 2, 3], weights=[50, 20, 10, 10, 6, 4])[0]
        seller = random.choice(sellers)
        status = random.choice(statuses)
        views = random.randint(0, 400)
        desc = f"[SEED]《{title}》— 一本值得反复阅读的好书。品相良好，内页干净无笔记。（种子批次 #{i+1}）"
        row = (
            f"('{escape_sql(title)}', '{escape_sql(author)}', '{isbn}', {cat_id}, "
            f"'{escape_sql(cover)}', '{escape_sql(desc)}', {price}, {orig}, "
            f"{cond}, {stock}, {seller}, {status}, {views}, 0)"
        )
        book_rows.append(row)

    lines.append(",\n".join(book_rows) + ";")

    wanted_rows = []
    wi = 0
    for title, author, isbn, desc in base.WANTED_SEEDS:
        if isbn not in allowed_isbn:
            continue
        digits = "".join(c for c in str(isbn) if c.isdigit())
        cover = cover_cache.get(digits) or _resolve_google_cover(isbn, api_key)
        if not cover:
            continue
        cover_cache[digits] = cover
        uid = random.choice(sellers)
        max_price = round(random.uniform(8, 60), 2)
        status = random.choice([1, 1, 1, 1, 2])
        views = random.randint(0, 350)
        wi += 1
        full_desc = f"[SEED]{desc}（种子批次 #{wi}）"
        row = (
            f"({uid}, '{escape_sql(title)}', '{escape_sql(author)}', '{isbn}', "
            f"'{escape_sql(cover)}', '{escape_sql(full_desc)}', {max_price}, {status}, {views}, 0)"
        )
        wanted_rows.append(row)

    if wanted_rows:
        lines.append(
            "\nINSERT INTO `book_wanted` (`user_id`, `book_title`, `author`, `isbn`, "
            "`cover_image`, `description`, `max_price`, `status`, `view_count`, `deleted`) VALUES"
        )
        lines.append(",\n".join(wanted_rows) + ";")
    else:
        lines.append(
            f"\n-- 无求购 INSERT：WANTED_SEEDS 与当前 {len(book_entries)} 本书的 ISBN 无交集。"
        )

    order_pool = [b[2] for b in book_entries]
    order_book_isbns = random.sample(order_pool, min(30, len(order_pool)))
    buyers = [2, 3, 4, 5, 6]
    lines.append(f"\n-- 模拟订单（至多 {len(order_book_isbns)} 条，ISBN 均来自本书单）")
    lines.append(
        "INSERT INTO `order` (`order_no`, `buyer_id`, `seller_id`, `book_id`, "
        "`quantity`, `total_price`, `status`, `payment_time`, `delivery_time`, "
        "`finish_time`, `remark`, `create_time`, `update_time`, `deleted`) VALUES"
    )

    order_rows = []
    for i, isbn in enumerate(order_book_isbns):
        buyer = random.choice(buyers)
        qty = 1
        status = random.choices([1, 2, 3, 4, 5], weights=[10, 20, 20, 40, 10])[0]
        now = "NOW()"
        pay_time = "NOW()" if status >= 2 else "NULL"
        ship_time = "NOW()" if status >= 3 else "NULL"
        done_time = "NOW()" if status >= 4 else "NULL"
        order_no = f"SEED{random.randint(1000, 9999)}{i:03d}"
        row = (
            f"('{order_no}', {buyer}, "
            f"(SELECT seller_id FROM book WHERE isbn='{isbn}' AND deleted=0 LIMIT 1), "
            f"(SELECT book_id FROM book WHERE isbn='{isbn}' AND deleted=0 LIMIT 1), "
            f"{qty}, "
            f"(SELECT COALESCE(price, 0) FROM book WHERE isbn='{isbn}' AND deleted=0 LIMIT 1), "
            f"{status}, {pay_time}, {ship_time}, {done_time}, "
            f"'种子订单 #{i+1}', {now}, {now}, 0)"
        )
        order_rows.append(row)

    lines.append(",\n".join(order_rows) + ";")
    lines.append("\nCOMMIT;")
    lines.append(f"\n-- Total: {len(book_rows)} books + {len(wanted_rows)} wanted + {len(order_rows)} orders")

    sql = "\n".join(lines) + "\n"
    with open(OUTPUT, "w", encoding="utf-8") as f:
        f.write(sql)

    print(f"\nDone → {OUTPUT}")
    print(f"  {len(book_rows)} books（Google Books 封面，上限 {max_books}）")
    print(f"  {len(wanted_rows)} wanted items")
    print(f"  {len(order_rows)} orders")


if __name__ == "__main__":
    generate()
