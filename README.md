# Order System 订单管理系统

基于 **SpringBoot3 + MyBatis + MySQL + Redis + RabbitMQ + Vue3** 的前后端分离电商订单系统，支持 Docker Compose 一键部署。

**在线演示**：http://8.156.66.224:81
测试账号：`admin / admin123`（管理员）、`user1 / user123`（普通用户）

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.3、MyBatis、MySQL 8.0、Redis 7、RabbitMQ 3、JWT、BCrypt |
| 前端 | Vue 3（Composition API）、Vite 5、Element Plus、Pinia、Vue Router、Axios |
| 部署 | Docker Compose 五容器编排（MySQL / Redis / RabbitMQ / 后端 / 前端 Nginx） |

## 功能模块

- **用户**：注册 / 登录（JWT 无状态认证，BCrypt 密码加密），USER / ADMIN 角色权限控制
- **商品**：商品浏览、详情、分类筛选；管理员商品与分类 CRUD
- **购物车**：增删改查、库存校验、数量累加
- **订单**：购物车下单、取消订单（恢复库存）、订单状态流转（管理员）

## 核心设计

### 1. 下单事务与防超卖

```sql
UPDATE product SET stock = stock - #{quantity}
WHERE id = #{id} AND stock >= #{quantity}
```

- 条件更新 SQL 原子扣减库存，并发下不会超卖（影响行数为 0 视为库存不足，抛异常回滚）
- `@Transactional` 保证「扣库存 → 创建订单 → 批量插入订单项 → 清购物车」整体原子性
- 订单项保存**商品名称与价格快照**，并校验用户确认价与数据库最新价一致，防止改价纠纷

### 2. Redis 商品缓存

- Cache-Aside 模式：读缓存 → 未命中查库 → 回写缓存（TTL 30min）
- **防缓存穿透**：不存在的商品缓存空值占位符 `"NULL"`
- 商品更新 / 删除时**删除缓存**而非更新缓存，规避并发写脏数据
- 解决 `LocalDateTime` 的 Redis JSON 序列化问题（Jackson JSR310 + DefaultTyping）

### 3. RabbitMQ 异步通知 + 死信队列

```
下单成功 → order.exchange (Direct) → order.notify.queue → 消费者发送通知
                                        ↓ TTL 30s 超时 / 拒收
                                  order.dlx.exchange → order.notify.dlx.queue
```

- 订单创建后异步发送通知，MQ 故障不影响下单主流程（try-catch 兜底）
- 死信队列承接超时 / 被拒消息，避免消息丢失

## 数据库设计

`user`、`category`、`product`、`cart_item`、`order`、`order_item` 六张表，订单与订单项一对多，订单项冗余商品快照字段。建表脚本见 [sql/init.sql](sql/init.sql)。

## 快速启动

```bash
# 一键启动（首次构建约需几分钟）
docker-compose up -d --build

# 前端 http://localhost:81  后端API http://localhost:8080
```

本地开发：

```bash
# 后端（需本地 MySQL/Redis/RabbitMQ，配置见 application.properties）
cd order-backend && mvn spring-boot:run

# 前端
cd order-frontend && npm install && npm run dev
```

## 项目结构

```
order-system/
├── docker-compose.yml       # 五容器编排
├── sql/init.sql             # 建表 + 种子数据
├── order-backend/           # SpringBoot3 后端
│   └── src/main/java/com/atguigu/order/
│       ├── controller/      # REST 接口
│       ├── service/         # 业务逻辑（事务、防超卖、缓存）
│       ├── mapper/          # MyBatis 接口 + XML
│       ├── config/          # Redis/RabbitMQ/CORS/拦截器配置
│       ├── consumer/        # MQ 消费者
│       └── common/          # 统一响应、全局异常、JWT 工具
└── order-frontend/          # Vue3 前端
    └── src/
        ├── views/           # 页面
        ├── api/             # 接口封装
        ├── stores/          # Pinia 状态
        └── utils/request.js # Axios 拦截器（自动携带 Token）
```
