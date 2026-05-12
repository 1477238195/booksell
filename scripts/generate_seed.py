#!/usr/bin/env python3
"""
生成二手书交易平台种子数据 SQL（输出 recommend_seed_200_fixed.sql）

封面策略（避免大量「暂无封面」内联 SVG）：
1. 对 Open Library 用 GET 拉取封面字节，校验 JPEG/PNG/WebP 魔数 + 最小体积（OL 的 HEAD 常误判无图）。
2. 依次尝试 -M.jpg、-L.jpg。
3. 仍失败：默认使用 picsum.photos 种子图（短 URL、真实照片）；若需旧版 SVG 设环境变量 SEED_COVER_FALLBACK=svg。

环境变量：OL_MIN_COVER_BYTES、OL_REQUEST_PAUSE_S、SEED_COVER_FALLBACK、SEED_OFFLINE=1、
  SEED_OL_ONLY_MAX_BOOKS（默认 25：只导出 Open Library 有真封面的前 N 本书，无图条目不写 SQL）
"""

import hashlib
import json
import random
import urllib.request
import urllib.error
import urllib.parse
import sys
import os
import time

OUTPUT = os.path.join(os.path.dirname(__file__) or ".", "recommend_seed_200_fixed.sql")

# ============ 200 本真实书籍（书名 / 作者 / 真实 ISBN / 分类ID）============
BOOK_SEEDS = [
    # ── 文学小说 (cat 2) ──
    ("活着", "余华", "9787530215319", 2),
    ("百年孤独", "加西亚·马尔克斯", "9787544253994", 2),
    ("围城", "钱锺书", "9787020024759", 2),
    ("红楼梦", "曹雪芹", "9787020002207", 2),
    ("平凡的世界", "路遥", "9787530212004", 2),
    ("三体", "刘慈欣", "9787536692930", 2),
    ("三体Ⅱ·黑暗森林", "刘慈欣", "9787536693968", 2),
    ("三体Ⅲ·死神永生", "刘慈欣", "9787536693999", 2),
    ("挪威的森林", "村上春树", "9787532725694", 2),
    ("海边的卡夫卡", "村上春树", "9787532726004", 2),
    ("1Q84", "村上春树", "9787544248808", 2),
    ("追风筝的人", "卡勒德·胡赛尼", "9787208061644", 2),
    ("灿烂千阳", "卡勒德·胡赛尼", "9787208072107", 2),
    ("群山回唱", "卡勒德·胡赛尼", "9787208109887", 2),
    ("解忧杂货店", "东野圭吾", "9787544270878", 2),
    ("白夜行", "东野圭吾", "9787544242516", 2),
    ("嫌疑人X的献身", "东野圭吾", "9787544241694", 2),
    ("恶意", "东野圭吾", "9787544244145", 2),
    ("放学后", "东野圭吾", "9787544243117", 2),
    ("悟空传", "今何在", "9787544251785", 2),
    ("黄金时代", "王小波", "9787532145683", 2),
    ("沉默的大多数", "王小波", "9787532148028", 2),
    ("边城", "沈从文", "9787020022221", 2),
    ("呼兰河传", "萧红", "9787020002221", 2),
    ("骆驼祥子", "老舍", "9787020022238", 2),
    ("四世同堂", "老舍", "9787020022245", 2),
    ("呐喊", "鲁迅", "9787020022252", 2),
    ("朝花夕拾", "鲁迅", "9787020022269", 2),
    ("倾城之恋", "张爱玲", "9787530216026", 2),
    ("半生缘", "张爱玲", "9787530214879", 2),
    ("小团圆", "张爱玲", "9787530214886", 2),

    # ── 科幻奇幻 (cat 9) ──
    ("沙丘", "弗兰克·赫伯特", "9787536493782", 9),
    ("银河帝国：基地", "艾萨克·阿西莫夫", "9787539945422", 9),
    ("1984", "乔治·奥威尔", "9787530210291", 9),
    ("动物农场", "乔治·奥威尔", "9787532725175", 9),
    ("美丽新世界", "阿道司·赫胥黎", "9787532757053", 9),
    ("安德的游戏", "奥森·斯科特·卡德", "9787536489105", 9),
    ("神经漫游者", "威廉·吉布森", "9787544275743", 9),
    ("海伯利安", "丹·西蒙斯", "9787544275781", 9),
    ("仿生人会梦见电子羊吗", "菲利普·迪克", "9787544766838", 9),
    ("你一生的故事", "特德·姜", "9787544769662", 9),
    ("火星救援", "安迪·威尔", "9787544765176", 9),
    ("黑暗的左手", "厄休拉·勒古恩", "9787536493768", 9),
    ("地海巫师", "厄休拉·勒古恩", "9787536493775", 9),
    ("华氏451", "雷·布拉德伯里", "9787532765997", 9),
    ("时间机器", "H.G.威尔斯", "9787532765980", 9),
    ("星际迷航：红衫", "约翰·斯卡尔齐", "9787536493751", 9),
    ("流浪地球", "刘慈欣", "9787536493799", 9),
    ("球状闪电", "刘慈欣", "9787536493805", 9),

    # ── 历史人文 (cat 11) ──
    ("万历十五年", "黄仁宇", "9787108009821", 11),
    ("人类简史", "尤瓦尔·赫拉利", "9787508638027", 11),
    ("未来简史", "尤瓦尔·赫拉利", "9787508672069", 11),
    ("枪炮、病菌与钢铁", "贾雷德·戴蒙德", "9787532125685", 11),
    ("明朝那些事儿·壹", "当年明月", "9787213049422", 11),
    ("全球通史", "斯塔夫里阿诺斯", "9787301098615", 11),
    ("中国大历史", "黄仁宇", "9787108010360", 11),
    ("叫魂", "孔飞力", "9787108010377", 11),
    ("国史大纲", "钱穆", "9787100017640", 11),
    ("菊与刀", "鲁思·本尼迪克特", "9787100017657", 11),
    ("史记", "司马迁", "9787101003086", 11),
    ("资治通鉴", "司马光", "9787101003093", 11),
    ("乌合之众", "古斯塔夫·勒庞", "9787511005809", 11),
    ("论中国", "亨利·基辛格", "9787508674155", 11),
    ("耶路撒冷三千年", "西蒙·蒙蒂菲奥里", "9787513909624", 11),
    ("苏东坡传", "林语堂", "9787540447885", 11),

    # ── 科学自然 (cat 12) ──
    ("时间简史", "斯蒂芬·霍金", "9787535732309", 12),
    ("果壳中的宇宙", "斯蒂芬·霍金", "9787535734372", 12),
    ("大设计", "斯蒂芬·霍金", "9787535747525", 12),
    ("自私的基因", "理查德·道金斯", "9787508638034", 12),
    ("上帝掷骰子吗", "曹天元", "9787530123685", 12),
    ("从一到无穷大", "乔治·伽莫夫", "9787030444561", 12),
    ("物种起源", "查尔斯·达尔文", "9787100103503", 12),
    ("昆虫记", "让-亨利·法布尔", "9787530112342", 12),
    ("寂静的春天", "蕾切尔·卡森", "9787532758944", 12),
    ("基因组：人种自传23章", "马特·里德利", "9787535747419", 12),
    ("万物简史", "比尔·布莱森", "9787535747396", 12),
    ("数学之美", "吴军", "9787115374067", 12),
    ("信息简史", "詹姆斯·格雷克", "9787115385806", 12),
    ("深度学习", "伊恩·古德费洛", "9787115461476", 12),
    ("人工智能简史", "尼克", "9787115471284", 12),
    ("思考，快与慢", "丹尼尔·卡尼曼", "9787508638041", 12),

    # ── 哲学思想 (cat 13) ──
    ("苏菲的世界", "乔斯坦·贾德", "9787506341271", 13),
    ("存在与时间", "海德格尔", "9787100040981", 13),
    ("纯粹理性批判", "康德", "9787100021234", 13),
    ("查拉图斯特拉如是说", "尼采", "9787100021241", 13),
    ("理想国", "柏拉图", "9787100021258", 13),
    ("作为意志和表象的世界", "叔本华", "9787100021265", 13),
    ("社会契约论", "卢梭", "9787100021272", 13),
    ("论自由", "约翰·密尔", "9787100021289", 13),
    ("西方哲学史", "罗素", "9787100021296", 13),
    ("中国哲学简史", "冯友兰", "9787108009807", 13),
    ("沉思录", "马可·奥勒留", "9787100103459", 13),
    ("人生的智慧", "叔本华", "9787532135685", 13),
    ("道德经", "老子", "9787101003055", 13),
    ("论语", "孔子", "9787101003109", 13),
    ("庄子", "庄周", "9787101003079", 13),

    # ── 商业经济 (cat 1) ──
    ("国富论", "亚当·斯密", "9787100103473", 1),
    ("资本论", "卡尔·马克思", "9787100103480", 1),
    ("经济学原理：微观经济学", "曼昆", "9787301150894", 1),
    ("穷查理宝典", "查理·芒格", "9787508661902", 1),
    ("巴菲特致股东的信", "沃伦·巴菲特", "9787111407010", 1),
    ("从0到1", "彼得·蒂尔", "9787508651125", 1),
    ("精益创业", "埃里克·莱斯", "9787508661896", 1),
    ("创新者的窘境", "克莱顿·克里斯坦森", "9787508633558", 1),
    ("原则", "瑞·达利欧", "9787508684031", 1),
    ("黑天鹅", "纳西姆·塔勒布", "9787508611237", 1),
    ("反脆弱", "纳西姆·塔勒布", "9787508638089", 1),
    ("影响力", "罗伯特·西奥迪尼", "9787559612519", 1),
    ("定位", "艾·里斯", "9787111526353", 1),
    ("卓有成效的管理者", "彼得·德鲁克", "9787111545262", 1),

    # ── 计算机/编程 (cat 4) ──
    ("深入理解计算机系统", "兰德尔·布莱恩特", "9787111544937", 4),
    ("算法导论", "科尔曼", "9787111407011", 4),
    ("设计模式", "GoF", "9787111161905", 4),
    ("代码大全", "史蒂夫·麦康奈尔", "9787121256530", 4),
    ("重构", "马丁·福勒", "9787115402219", 4),
    ("程序员修炼之道", "安德鲁·亨特", "9787121379932", 4),
    ("Clean Code", "罗伯特·马丁", "9780132350884", 4),
    ("计算机程序设计艺术", "高德纳", "9780201485417", 4),
    ("编译原理", "阿尔弗雷德·阿霍", "9787111251217", 4),
    ("TCP/IP详解", "史蒂文斯", "9787111373254", 4),
    ("UNIX环境高级编程", "史蒂文斯", "9787115352118", 4),
    ("鸟哥的Linux私房菜", "鸟哥", "9787115352125", 4),
    ("Python编程：从入门到实践", "埃里克·马瑟斯", "9787115428028", 4),
    ("流畅的Python", "卢西亚诺·拉马略", "9787115454156", 4),
    ("Effective Java", "约书亚·布洛赫", "9780134685991", 4),
    ("JavaScript高级程序设计", "马特·弗里斯比", "9787115545381", 4),
    ("高性能MySQL", "施瓦茨", "9787115369208", 4),
    ("Redis设计与实现", "黄健宏", "9787111537625", 4),
    ("Spring实战", "克雷格·沃尔斯", "9787115417268", 4),
    ("Docker技术入门与实战", "杨保华", "9787111539421", 4),

    # ── 心理学 (cat 5) ──
    ("社会心理学", "戴维·迈尔斯", "9787115539137", 5),
    ("心理学与生活", "理查德·格里格", "9787115539144", 5),
    ("自卑与超越", "阿德勒", "9787532135692", 5),
    ("梦的解析", "弗洛伊德", "9787100103442", 5),
    ("乌合之众", "古斯塔夫·勒庞", "9787511005793", 5),
    ("自控力", "凯利·麦格尼格尔", "9787508685274", 5),
    ("非暴力沟通", "马歇尔·卢森堡", "9787508647531", 5),
    ("亲密关系", "罗兰·米勒", "9787115539151", 5),
    ("少有人走的路", "M·斯科特·派克", "9787532135708", 5),
    ("被讨厌的勇气", "岸见一郎", "9787508674162", 5),

    # ── 艺术设计 (cat 7) ──
    ("设计中的设计", "原研哉", "9787209037778", 7),
    ("写给大家看的设计书", "罗宾·威廉姆斯", "9787115539168", 7),
    ("艺术的故事", "贡布里希", "9787108009814", 7),
    ("美的历程", "李泽厚", "9787108009838", 7),
    ("色彩设计的原理", "伊達千代", "9787508685243", 7),
    ("版面设计的原理", "伊達千代", "9787508685250", 7),
    ("字型散步", "柯志杰", "9787508685267", 7),
    ("设计心理学", "唐纳德·诺曼", "9787115539175", 7),

    # ── 教育 (cat 3) ──
    ("如何阅读一本书", "莫提默·艾德勒", "9787100040974", 3),
    ("学会提问", "尼尔·布朗", "9787111537632", 3),
    ("刻意练习", "安德斯·艾利克森", "9787111537649", 3),
    ("终身成长", "卡罗尔·德韦克", "9787111537656", 3),
    ("认知天性", "彼得·布朗", "9787111537663", 3),
    ("学习之道", "芭芭拉·奥克利", "9787111537670", 3),
    ("把时间当作朋友", "李笑来", "9787121256523", 3),
    ("金字塔原理", "芭芭拉·明托", "9787544248815", 3),
    ("高效能人士的七个习惯", "史蒂芬·柯维", "9787500648680", 3),
    ("搞定", "戴维·艾伦", "9787508685281", 3),

    # ── 生活 (cat 8) ──
    ("断舍离", "山下英子", "9787544248822", 8),
    ("怦然心动的人生整理魔法", "近藤麻理惠", "9787544248839", 8),
    ("小王子", "圣埃克苏佩里", "9787020042494", 8),
    ("你今天真好看", "莉兹·克里莫", "9787544248846", 8),
    ("我决定简单地生活", "佐佐木典士", "9787544248853", 8),

    # ── 法律政治 (cat 10) ──
    ("论法的精神", "孟德斯鸠", "9787100103527", 10),
    ("联邦党人文集", "汉密尔顿", "9787100021302", 10),
    ("正义论", "罗尔斯", "9787100103466", 10),
    ("政府论", "洛克", "9787100103510", 10),
    ("利维坦", "霍布斯", "9787100021333", 10),
    ("宪法学导论", "张千帆", "9787511838094", 10),
    ("法治及其本土资源", "苏力", "9787301083940", 10),

    # ── 医学健康 (cat 6) ──
    ("医学生物学", "陈主初", "9787117057035", 6),
    ("黄帝内经", "佚名", "9787101003062", 6),
    ("本草纲目", "李时珍", "9787101003048", 6),
    ("惊人的超慢跑", "梅方久仁子", "9787508690117", 6),
    ("睡眠革命", "尼克·利特尔黑尔斯", "9787508690124", 6),
    ("你是你吃出来的", "夏萌", "9787508690131", 6),
]

# ============ 40 条求购信息 ============
WANTED_SEEDS = [
    ("求购《红楼梦》脂评本", "曹雪芹", "9787020002207", "想找一套脂砚斋评点本，品相不限，价格合理即可。"),
    ("求购《百年孤独》首版中译本", "加西亚·马尔克斯", "9787544253994", "收藏用，希望书况较好，有护封更好。"),
    ("求购《三体》签名本", "刘慈欣", "9787536692930", "刘慈欣签名本优先，普通版也可，要求正版。"),
    ("求购《活着》精装版", "余华", "9787530215319", "精装版优先，平装也可，新旧不限。"),
    ("求购《围城》老版本", "钱锺书", "9787020024759", "2000年以前的版本均可，品相无所谓。"),
    ("求购《平凡的世界》全套", "路遥", "9787530212004", "三册齐全，不散卖，正版即可。"),
    ("求购金庸全集", "金庸", "9787108009821", "希望收一套较完整的金庸武侠小说全集。"),
    ("求购《时间简史》插图版", "霍金", "9787535732309", "插图版优先，普通版也考虑。"),
    ("求购《万历十五年》", "黄仁宇", "9787108009821", "经典版本即可，内容完整无缺页。"),
    ("求购《人类简史》三部曲", "尤瓦尔·赫拉利", "9787508647357", "三本一起收，单本也可联系。"),
    ("求购《苏菲的世界》", "乔斯坦·贾德", "9787506341271", "任意版本均可，给孩子做哲学启蒙。"),
    ("求购《明朝那些事儿》全集", "当年明月", "9787213049422", "全七册最好，散本也收。"),
    ("求购《沉默的大多数》", "王小波", "9787532148028", "王小波的作品都想收，先从这本开始。"),
    ("求购《白夜行》", "东野圭吾", "9787544242516", "东野圭吾的书都收，优先白夜行。"),
    ("求购《嫌疑人X的献身》", "东野圭吾", "9787544241694", "收东野圭吾的代表作。"),
    ("求购《解忧杂货店》", "东野圭吾", "9787544270878", "品相好一些的优先。"),
    ("求购《1984》中英对照版", "乔治·奥威尔", "9787530210291", "中英对照版最好，普通中文版也行。"),
    ("求购《动物农场》", "乔治·奥威尔", "9787532725175", "任意版本均可，内容完整即可。"),
    ("求购《沙丘》系列", "弗兰克·赫伯特", "9787536493782", "六部曲齐全最好，单本也考虑。"),
    ("求购《基地》系列", "艾萨克·阿西莫夫", "9787539945422", "阿西莫夫基地系列，版本不限。"),
    ("求购《挪威的森林》日文原版", "村上春树", "9787532725694", "日文原版优先，中文版也可。"),
    ("求购《追风筝的人》", "卡勒德·胡赛尼", "9787208061644", "品相好的优先。"),
    ("求购《枪炮、病菌与钢铁》", "贾雷德·戴蒙德", "9787532125685", "对世界史感兴趣，收一本看看。"),
    ("求购《国富论》经典译本", "亚当·斯密", "9787100103473", "最好是严复译本或郭大力王亚南译本。"),
    ("求购《资本论》", "卡尔·马克思", "9787100103480", "全三卷最好。"),
    ("求购《理想国》", "柏拉图", "9787100021258", "商务印书版最佳。"),
    ("求购《沉思录》", "马可·奥勒留", "9787100103459", "何怀宏译本优先。"),
    ("求购《道德经》注本", "老子", "9787101003055", "王弼注本或其他经典注本均可。"),
    ("求购《社会心理学》第11版", "戴维·迈尔斯", "9787115539137", "教材用，版本越新越好。"),
    ("求购《哈利·波特》中文全套", "J.K.罗琳", "9787020033430", "人民文学出版社版本，全七册。"),
    ("求购《小王子》中法对照版", "圣埃克苏佩里", "9787020042494", "中法对照最佳，收藏用。"),
    ("求购《边城》沈从文", "沈从文", "9787020022221", "老版本优先，品相不限。"),
    ("求购《倾城之恋》", "张爱玲", "9787530216026", "张爱玲小说集均可。"),
    ("求购《骆驼祥子》", "老舍", "9787020022238", "经典版本即可。"),
    ("求购《呐喊》《彷徨》", "鲁迅", "9787020022252", "鲁迅小说集，两本一起收。"),
    ("求购《三体》英文版", "刘慈欣", "9780765377067", "想对照中英文阅读。"),
    ("求购数据结构和算法教材", "不限", "9787111407010", "适合自学，用Python或Java讲解均可。"),
    ("求购《设计中的设计》", "原研哉", "9787209037778", "原研哉的书都收。"),
    ("求购《怦然心动的人生整理魔法》", "近藤麻理惠", "9787544248808", "整理收纳类好书。"),
    ("求购《被讨厌的勇气》", "岸见一郎", "9787508647357", "阿德勒心理学入门。"),
]


# OL 对 HEAD 常不返回 image/* 或 405，误判为「无图」→ 大量 SVG。改为 GET + 魔数 + 最小字节数。
_MIN_COVER_BYTES = int(os.environ.get("OL_MIN_COVER_BYTES", "800"))
_PAUSE_S = float(os.environ.get("OL_REQUEST_PAUSE_S", "0.04"))
_COVER_URL_CACHE = {}


def _cover_body_ok(data: bytes) -> bool:
    if not data or len(data) < _MIN_COVER_BYTES:
        return False
    if data[:2] == b"\xff\xd8":
        return True
    if len(data) >= 8 and data[:8] == b"\x89PNG\r\n\x1a\n":
        return True
    if len(data) >= 12 and data[:4] == b"RIFF" and data[8:12] == b"WEBP":
        return True
    return False


def _fetch_cover_bytes(url: str, timeout: int = 10) -> bytes:
    try:
        req = urllib.request.Request(
            url,
            headers={
                "User-Agent": "alex-seed-generate/1.1 (+https://openlibrary.org)",
                "Accept": "image/*,*/*;q=0.8",
            },
        )
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status != 200:
                return b""
            return resp.read(262144)
    except Exception:
        return b""


def _openlibrary_cover_url(isbn: str, size: str = "M") -> str:
    digits = "".join(c for c in str(isbn) if c.isdigit())
    return f"https://covers.openlibrary.org/b/isbn/{digits}-{size}.jpg"


def _picsum_cover_url(isbn: str, title_hint: str) -> str:
    """稳定、短 URL 的占位图（真实照片），避免 data: SVG 过长且仍算「有图」。"""
    h = hashlib.md5(f"{isbn}|{title_hint}".encode("utf-8")).hexdigest()[:16]
    return f"https://picsum.photos/seed/bkseed{h}/400/520"


_ol_hits = 0
_ol_total = 0


def _resolve_openlibrary_only(isbn: str):
    """
    仅当 Open Library M/L 封面通过字节校验时返回 URL，否则 None。
    成功时写入 _COVER_URL_CACHE，供求购同 ISBN 复用。
    """
    global _ol_hits, _ol_total
    digits = "".join(c for c in str(isbn) if c.isdigit())
    if not digits:
        return None
    if digits in _COVER_URL_CACHE and "openlibrary.org" in _COVER_URL_CACHE[digits]:
        return _COVER_URL_CACHE[digits]
    for size in ("M", "L"):
        _ol_total += 1
        if _PAUSE_S > 0:
            time.sleep(_PAUSE_S)
        url = _openlibrary_cover_url(digits, size)
        body = _fetch_cover_bytes(url)
        if _cover_body_ok(body):
            _ol_hits += 1
            _COVER_URL_CACHE[digits] = url
            return url
    return None


def gen_cover_url(isbn: str, title_hint: str = "") -> str:
    """封面：Open Library GET 校验（M→L）；失败则用 picsum 种子图（真实照片 URL，短且稳定）。"""
    global _ol_hits, _ol_total
    digits = "".join(c for c in str(isbn) if c.isdigit())
    cache_key = digits or isbn
    if cache_key in _COVER_URL_CACHE:
        return _COVER_URL_CACHE[cache_key]

    if os.environ.get("SEED_OFFLINE", "").strip() in ("1", "true", "yes"):
        u = _picsum_cover_url(isbn, title_hint)
        _COVER_URL_CACHE[cache_key] = u
        return u

    _ol_total += 1
    if not digits:
        u = _picsum_cover_url(isbn, title_hint)
        _COVER_URL_CACHE[cache_key] = u
        return u

    for size in ("M", "L"):
        if _PAUSE_S > 0:
            time.sleep(_PAUSE_S)
        url = _openlibrary_cover_url(digits, size)
        body = _fetch_cover_bytes(url)
        if _cover_body_ok(body):
            _ol_hits += 1
            _COVER_URL_CACHE[cache_key] = url
            return url

    if os.environ.get("SEED_COVER_FALLBACK", "").lower() == "svg":
        u = _svg_placeholder(title_hint)
    else:
        u = _picsum_cover_url(isbn, title_hint)
    _COVER_URL_CACHE[cache_key] = u
    return u


def _svg_placeholder(title: str) -> str:
    """生成一个包含中文书名的 SVG data URI 占位图"""
    display = title[:8] + ("…" if len(title) > 8 else "")
    lines = [display[i:i+4] for i in range(0, len(display), 4)]
    tspans = "".join(
        f'<tspan x="200" dy="{30 if i == 0 else 34}">{line}</tspan>'
        for i, line in enumerate(lines)
    )
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="400" height="520" viewBox="0 0 400 520">
  <rect width="400" height="520" fill="#f5f0e8"/>
  <rect x="10" y="10" width="380" height="500" rx="6" fill="none" stroke="#e8e0d5" stroke-width="1.5"/>
  <g fill="#6b5e4f" font-family="PingFang SC,Noto Sans SC,Microsoft YaHei,sans-serif" font-size="26" text-anchor="middle">
    {tspans}
  </g>
  <line x1="80" y1="360" x2="320" y2="360" stroke="#e8e0d5" stroke-width="1"/>
  <text x="200" y="400" fill="#a89880" font-family="PingFang SC,Noto Sans SC,Microsoft YaHei,sans-serif" font-size="14" text-anchor="middle">暂 无 封 面</text>
</svg>'''
    encoded = urllib.parse.quote(svg)
    return f"data:image/svg+xml,{encoded}"


def escape_sql(s: str) -> str:
    return s.replace("\\", "\\\\").replace("'", "\\'")


def generate():
    print("Generating seed SQL with real ISBNs...")

    max_ol_books = int(os.environ.get("SEED_OL_ONLY_MAX_BOOKS", "25"))
    offline = os.environ.get("SEED_OFFLINE", "").strip() in ("1", "true", "yes")

    sellers = [2, 3, 4, 5, 6]  # user IDs
    conditions = [1, 2, 2, 3, 3, 3, 4]
    statuses = [1] * 8 + [0, 2]

    global _ol_hits, _ol_total
    _ol_hits = 0
    _ol_total = 0
    _COVER_URL_CACHE.clear()

    # 仅保留 Open Library 有真封面的书（最多 max_ol_books 本）；SEED_OFFLINE 时仍用原逻辑 + picsum
    if offline:
        book_entries = [(t, a, i, c, gen_cover_url(i, t)) for t, a, i, c in BOOK_SEEDS]
    else:
        ol_candidates = []
        for title, author, isbn, cat_id in BOOK_SEEDS:
            cu = _resolve_openlibrary_only(isbn)
            if cu:
                ol_candidates.append((title, author, isbn, cat_id, cu))
        book_entries = ol_candidates[:max_ol_books]
        if len(book_entries) < max_ol_books:
            print(
                f"警告：Open Library 有封面的书仅 {len(book_entries)} 本（目标 {max_ol_books}），"
                "已全数导出。可调低 OL_MIN_COVER_BYTES 或检查网络。",
                file=sys.stderr,
            )

    if not book_entries:
        print("错误：没有可导出的图书记录（离线请设 SEED_OFFLINE=1；联网请检查 OL）", file=sys.stderr)
        sys.exit(1)

    allowed_isbn = {b[2] for b in book_entries}

    lines = []
    if offline:
        lines.append(
            "-- 二手书交易平台种子数据（SEED_OFFLINE=1：全书单 + picsum 封面；不设离线时仅导出 OL 有真图的书，默认最多 25 本）"
        )
    else:
        lines.append(
            "-- 二手书交易平台种子数据：仅含 Open Library 有真封面的书籍（默认最多 25 本），无 picsum / 无 SVG 占位。"
        )
    lines.append("-- Generated by scripts/generate_seed.py")
    lines.append(f"-- Books: {len(book_entries)}, Wanted rows: (见下方 INSERT 或注释)")
    lines.append("SET NAMES utf8mb4;\n")
    lines.append("START TRANSACTION;\n")

    # ── books ──
    lines.append("INSERT INTO `book` (`title`, `author`, `isbn`, `category_id`, "
                 "`cover_image`, `description`, `price`, `original_price`, "
                 "`condition`, `stock`, `seller_id`, `status`, `view_count`, `deleted`) VALUES")

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
        row = (f"('{escape_sql(title)}', '{escape_sql(author)}', '{isbn}', {cat_id}, "
               f"'{cover}', '{escape_sql(desc)}', {price}, {orig}, "
               f"{cond}, {stock}, {seller}, {status}, {views}, 0)")
        book_rows.append(row)

    lines.append(",\n".join(book_rows) + ";")

    # ── wanted：仅 ISBN 在本书单内；封面与书一致（缓存中的 OL URL）──
    wanted_rows = []
    wi = 0
    for title, author, isbn, desc in WANTED_SEEDS:
        if isbn not in allowed_isbn:
            continue
        if offline:
            cover = gen_cover_url(isbn, title)
        else:
            cover = _COVER_URL_CACHE.get("".join(c for c in str(isbn) if c.isdigit()))
            if not cover:
                cover = _resolve_openlibrary_only(isbn)
        if not cover:
            continue
        uid = random.choice(sellers)
        max_price = round(random.uniform(8, 60), 2)
        status = random.choice([1, 1, 1, 1, 2])
        views = random.randint(0, 350)
        wi += 1
        full_desc = f"[SEED]{desc}（种子批次 #{wi}）"
        row = (f"({uid}, '{escape_sql(title)}', '{escape_sql(author)}', '{isbn}', "
               f"'{cover}', '{escape_sql(full_desc)}', {max_price}, {status}, {views}, 0)")
        wanted_rows.append(row)

    if wanted_rows:
        lines.append("\nINSERT INTO `book_wanted` (`user_id`, `book_title`, `author`, `isbn`, "
                     "`cover_image`, `description`, `max_price`, `status`, `view_count`, `deleted`) VALUES")
        lines.append(",\n".join(wanted_rows) + ";")
    else:
        lines.append(
            f"\n-- 无求购 INSERT：WANTED_SEEDS 与当前 {len(book_entries)} 本书的 ISBN 无交集（可编辑 WANTED_SEEDS 对齐）。"
        )

    # ── orders ──
    # 从已插入的书籍中选取一些生成订单（通过 ISBN 关联）
    order_pool = [b[2] for b in book_entries]
    order_book_isbns = random.sample(order_pool, min(30, len(order_pool)))
    buyers = [2, 3, 4, 5, 6]
    lines.append(f"\n-- 模拟订单（至多 {len(order_book_isbns)} 条，ISBN 均来自本书单）")
    lines.append("INSERT INTO `order` (`order_no`, `buyer_id`, `seller_id`, `book_id`, "
                 "`quantity`, `total_price`, `status`, `payment_time`, `delivery_time`, "
                 "`finish_time`, `remark`, `create_time`, `update_time`, `deleted`) VALUES")

    order_rows = []
    for i, isbn in enumerate(order_book_isbns):
        buyer = random.choice(buyers)
        qty = 1
        status = random.choices([1, 2, 3, 4, 5], weights=[10, 20, 20, 40, 10])[0]
        now = "NOW()"
        pay_time = "NOW()" if status >= 2 else "NULL"
        ship_time = "NOW()" if status >= 3 else "NULL"
        done_time = "NOW()" if status >= 4 else "NULL"
        order_no = f"SEED{random.randint(1000,9999)}{i:03d}"
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
    print(f"  {len(book_rows)} books（Open Library 有真封面，上限 {max_ol_books}）")
    print(f"  {len(wanted_rows)} wanted items")
    if os.environ.get("SEED_OFFLINE", "").strip() in ("1", "true", "yes"):
        print("  封面：SEED_OFFLINE=1，全部使用 picsum（未请求 Open Library）")
    else:
        print(
            f"  导出 {len(book_rows)} 本书（仅 OL 有真图，最多 {max_ol_books}）；"
            f"扫描时 OL 成功 {_ol_hits} 次 / HTTP 探测 {_ol_total} 次；求购 {len(wanted_rows)} 条；订单 {len(order_rows)} 条"
        )


if __name__ == "__main__":
    generate()
