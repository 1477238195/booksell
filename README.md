# 二手书交易平台

## 项目简介

这是一个基于 **Spring Boot + Vue 3** 开发的二手书交易平台，为用户提供便捷的二手书籍交易服务。平台支持书籍发布、在线浏览、分类管理、订单交易、求购信息发布以及站内实时消息等核心功能。

### 核心特点

- 📚 **书籍管理** - 发布、浏览、编辑、上下架书籍
- 🔍 **智能搜索** - 按书名、分类、价格、新旧程度筛选
- 🛒 **订单系统** - 完整的交易流程管理
- 💬 **实时消息** - 基于 WebSocket 的站内私信
- 🏷️ **分类管理** - 多级书籍分类体系
- 📢 **求购发布** - 用户发布求购需求
- 🔐 **权限认证** - JWT Token 安全认证

---

## 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.x | 核心框架 |
| MySQL | 5.7+ | 数据库 |
| MyBatis | 2.1+ | 持久层框架 |
| tk.mybatis | 通用Mapper | 简化CRUD |
| PageHelper | 分页插件 | 分页查询 |
| JWT | EC算法 | 身份认证 |
| WebSocket | - | 实时通信 |
| Swagger 2 | - | API文档 |
| Lombok | - | 简化代码 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5+ | 核心框架 |
| Vite | 6.2+ | 构建工具 |
| Element Plus | 2.9+ | UI组件库 |
| Pinia | 3.0+ | 状态管理 |
| Vue Router | 4.5+ | 路由管理 |
| Axios | - | HTTP客户端 |

---

## 功能模块

### 1. 书籍管理 ✅

**功能说明：**
- 书籍列表：分页展示所有在售书籍，支持按书名、分类、状态筛选
- 发布书籍：用户发布二手书，填写书名、作者、ISBN、价格、新旧程度、描述等
- 我的书籍：查看和管理自己发布的书籍，支持编辑、上下架、删除
- 书籍详情：查看书籍完整信息，自动统计浏览次数
- 分类管理：多级分类体系，支持分类的增删改查

**核心代码：**
- 后端实体：[Book.java](src/main/java/com/coding/entity/Book.java)
- 后端控制器：[BookController.java](src/main/java/com/coding/controller/BookController.java)
- 前端API：[BookApi.js](admin/src/api/BookApi.js)
- 前端页面：
  - [书籍列表](admin/src/views/book/list.vue)
  - [发布书籍](admin/src/views/book/publish.vue)
  - [我的书籍](admin/src/views/book/my-books.vue)
  - [分类管理](admin/src/views/book/category.vue)

**书籍状态：**
- 0 - 已下架
- 1 - 在售
- 2 - 已售出

**新旧程度：**
- 1 - 全新
- 2 - 几乎全新
- 3 - 轻微使用痕迹
- 4 - 明显使用痕迹

### 2. 订单管理 🔧

**功能说明：**
- 创建订单：用户购买书籍时生成订单
- 订单列表：买家查看购买订单，卖家查看销售订单
- 订单状态：待支付、待发货、待收货、已完成、已取消
- 订单详情：查看订单完整信息和物流状态

**订单状态流转：**
```
待支付(1) → 待发货(2) → 待收货(3) → 已完成(4)
   ↓                                      ↓
   └─────────→ 已取消(5) ←────────────────┘
```

### 3. 求购信息 🔧

**功能说明：**
- 发布求购：用户发布想要购买的书籍需求
- 求购列表：浏览所有求购信息
- 我的求购：管理自己发布的求购信息
- 响应求购：卖家可以响应买家的求购需求

### 4. 站内消息 🔧

**功能说明：**
- 实时通信：基于 WebSocket 的即时消息推送
- 私信功能：买卖双方可以私信沟通
- 消息列表：查看收到的所有消息
- 消息提醒：新消息实时提醒

**WebSocket端点：**
```
ws://localhost:8888/ws/event
```

### 5. 用户系统 ✅

**功能说明：**
- 用户注册：支持普通用户和管理员角色注册
- 用户登录：JWT Token 认证，自动刷新
- 权限控制：基于角色的资源访问控制
- 个人信息：查看和编辑个人资料

**核心代码：**
- 后端控制器：[AuthController.java](src/main/java/com/coding/controller/auth/AuthController.java)
- 前端页面：
  - [登录页](admin/src/views/login/login.vue)
  - [注册页](admin/src/views/register/index.vue)

---

## 数据库设计

### 核心数据表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| user | 用户表 | user_id, username, password, role, email, phone |
| book | 书籍表 | book_id, title, author, isbn, category_id, price, condition, status |
| book_category | 书籍分类表 | category_id, name, parent_id, sort_order |
| order | 订单表 | order_id, buyer_id, seller_id, book_id, total_amount, status |
| book_wanted | 求购信息表 | wanted_id, user_id, title, description, target_price |
| message | 站内消息表 | message_id, sender_id, receiver_id, content, is_read |

**数据库初始化：** 项目根目录的 [init.sql](init.sql) 包含所有表结构和初始数据（15个分类 + 2个测试用户）

---

## 快速开始

### 1. 环境要求

- **JDK**: 1.8 或以上
- **Maven**: 3.6+
- **Node.js**: 20+
- **pnpm**: 9+
- **MySQL**: 5.7+
- **IDE**: IntelliJ IDEA（推荐）

### 2. 数据库初始化

执行项目根目录的 `init.sql` 脚本创建数据库表和初始数据。

**数据库连接信息：**
- 主机：123.6.146.2
- 端口：33306
- 数据库：szj
- 用户名：SZJ
- 密码：Szj123456

### 3. 启动后端

**方式一：使用 Maven**
```bash
cd springboot-vue-template-main
mvn clean install
mvn spring-boot:run
```

**方式二：使用 IDEA**
1. 打开 IntelliJ IDEA，导入项目
2. 找到主类：`src/main/java/com/coding/FoodApp.java`
3. 右键运行 `FoodApp.main()`
4. 默认端口：8888

**验证启动成功：**
- 后端服务：http://localhost:8888
- API文档：http://localhost:8888/swagger-ui.html

### 4. 启动前端

```bash
# 进入前端目录
cd admin

# 安装依赖（首次运行）
pnpm install

# 启动开发服务器
pnpm dev
```

**访问地址：** http://localhost:5173

**生产构建：**
```bash
pnpm build:prod
```

### 5. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user | user123 |

---

## 项目配置

### 后端配置

**数据库配置：** `src/main/resources/application-local.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://123.6.146.2:33306/szj
    username: SZJ
    password: Szj123456
```

**跨域配置：** `src/main/java/com/coding/config/WebConfig.java`
- 已允许所有域名跨域访问
- 支持 GET、POST、PUT、DELETE、OPTIONS 方法

**白名单配置：** `src/main/resources/application.yml`
```yaml
app:
  white-list:
    - /api/auth/login      # 登录接口
    - /api/auth/register   # 注册接口
```

### 前端配置

**API 代理配置：** `admin/vite.config.js`
```javascript
proxy: {
  '/admin-api': {
    target: 'http://localhost:8888',  // 后端服务地址
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/admin-api/, '')
  }
}
```

**环境变量：** `admin/.env.development`
```
VITE_APP_BASE_API = /admin-api
```

---

## API 接口

### 用户认证
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/info` - 获取当前用户信息

### 书籍管理
- `GET /api/book/page` - 分页查询书籍列表
- `POST /api/book/add` - 新增书籍
- `POST /api/book/update` - 更新书籍
- `GET /api/book/delete` - 删除书籍
- `GET /api/book/detail` - 查询书籍详情
- `POST /api/book/updateStatus` - 上架/下架书籍

### 书籍分类
- `GET /api/bookCategory/list` - 查询所有分类
- `POST /api/bookCategory/add` - 新增分类
- `POST /api/bookCategory/update` - 更新分类
- `GET /api/bookCategory/delete` - 删除分类

---

## 核心工具类

### 1. 密码工具类
**位置：** `src/main/java/com/coding/utils/PasswordUtil.java`
- `encode(String password)` - 密码加密
- `matches(String raw, String encoded)` - 密码验证

### 2. 响应工具类
**统一返回结构：**
```java
R.createBySuccess(data)           // 成功响应
R.createByErrorMessage("错误信息")  // 错误响应
```

### 3. HTTP工具类
**位置：** `src/main/java/com/coding/utils/HttpKit.java`
- `getUserId()` - 获取当前登录用户ID

### 4. 分页工具
- `PageResult<T>` - 分页结果封装
- `RequestPage` - 分页请求参数

---

## 项目结构

```
springboot-vue-template-main/
├── admin/                          # 前端Vue3项目
│   ├── src/
│   │   ├── api/                   # API接口定义
│   │   │   └── BookApi.js        # 书籍相关API
│   │   ├── components/            # 公共组件
│   │   ├── layout/                # 布局组件
│   │   ├── router/                # 路由配置
│   │   ├── store/                 # 状态管理
│   │   ├── views/                 # 页面视图
│   │   │   ├── book/             # 书籍管理页面
│   │   │   │   ├── list.vue      # 书籍列表
│   │   │   │   ├── publish.vue   # 发布书籍
│   │   │   │   ├── my-books.vue  # 我的书籍
│   │   │   │   └── category.vue  # 分类管理
│   │   │   ├── dashboard/        # 仪表盘
│   │   │   ├── login/            # 登录页
│   │   │   └── register/         # 注册页
│   │   └── utils/                # 工具函数
│   └── package.json              # 前端依赖配置
│
├── src/main/
│   ├── java/com/coding/
│   │   ├── common/               # 公共常量和配置
│   │   ├── config/               # Spring配置类
│   │   ├── controller/           # 控制器层
│   │   │   ├── BookController.java          # 书籍控制器
│   │   │   ├── BookCategoryController.java  # 分类控制器
│   │   │   └── auth/            # 认证相关接口
│   │   ├── entity/              # 实体类
│   │   │   ├── Book.java        # 书籍实体
│   │   │   ├── BookCategory.java # 分类实体
│   │   │   └── User.java        # 用户实体
│   │   ├── mapper/              # MyBatis Mapper接口
│   │   │   ├── BookMapper.java
│   │   │   └── BookCategoryMapper.java
│   │   ├── service/             # 业务逻辑层
│   │   │   ├── IBookService.java
│   │   │   └── impl/
│   │   │       └── BookServiceImpl.java
│   │   ├── vo/                  # 视图对象
│   │   ├── utils/               # 工具类
│   │   └── websocket/           # WebSocket处理器
│   └── resources/
│       ├── mapper/              # MyBatis XML映射文件
│       │   └── BookMapper.xml
│       ├── application.yml      # 主配置文件
│       └── application-local.yml # 本地环境配置
├── init.sql                      # 数据库初始化脚本
└── pom.xml                       # Maven依赖配置
```

---

## 常见问题

### 1. 数据库连接失败
- 检查数据库地址、端口、用户名、密码是否正确
- 确认数据库服务是否启动
- 检查防火墙设置

### 2. JWT Token验证失败
- 检查Token是否过期
- 确认请求头是否包含 `Authorization: Bearer <token>`

### 3. WebSocket连接失败
- 检查WebSocket端点地址
- 确认服务器WebSocket配置是否正确

### 4. 前端跨域问题
- 后端已配置CORS
- 检查 `WebConfig.java` 中的跨域配置

### 5. 启动后端时提示找不到主类
- 右键项目 → Maven → Reload Project
- 确认 JDK 版本配置正确

---

## 开发建议

1. 遵循RESTful API设计规范
2. 使用统一的响应格式
3. 实现全局异常处理
4. 添加接口参数校验
5. 编写单元测试
6. 使用事务管理
7. 实现日志记录
8. 注意SQL注入防护
9. 敏感信息加密存储
10. 定期数据备份

---

## 开发进度

### 已完成功能 ✅
- [x] 用户注册与登录
- [x] JWT 认证
- [x] 书籍管理（列表、发布、编辑、删除）
- [x] 书籍分类管理
- [x] 我的书籍
- [x] 书籍上下架

### 待开发功能 🔧
- [ ] 订单创建与管理
- [ ] 求购信息发布与响应
- [ ] 站内私信界面
- [ ] 支付集成（支付宝/微信）
- [ ] 物流跟踪
- [ ] 用户评价系统
- [ ] 推荐算法
- [ ] 数据统计和报表

---

## 联系方式

如有问题，请联系项目开发者。

---

**最后更新时间：** 2025-01-24
