# 旅行预订智能客服平台

基于 **Spring Cloud Alibaba 微服务** + **Spring AI Alibaba 多智能体** 的旅行预订智能客服系统。用户可通过自然语言查询订单、办理改签、咨询政策；系统通过多智能体路由分诊、Function Calling 调用业务微服务、RAG 检索知识库，完成端到端的智能应答。

> 个人全栈项目（前端 + 微服务后端 + AI 应用）。

---

##  核心特性

- **多智能体编排**：基于 Spring AI Alibaba Graph 实现 `Router 分诊 + 订单/改签/FAQ/兜底` 四专家协作，状态机驱动
- **Function Calling**：AI 通过工具调用跨服务发起真实业务操作（查订单、改签）
- **RAG 知识库**：PostgreSQL + pgvector 向量检索，结合 text-embedding-v3，回答政策类问题不编造
- **多轮对话**：Redis 存储短期上下文，支持省略指代（"那第二个呢"）
- **会话历史**：冷热分离——Redis 做 LLM 上下文窗口（TTL），PostgreSQL 永久存储会话历史（ChatGPT 式列表）
- **统一鉴权**：JWT + 网关全局校验 + userId 透传，支持"查我的订单"
- **限流熔断**：Sentinel 网关限流 + LLM 调用熔断降级，依赖故障时优雅降级

##  技术架构

```
                    ┌──────────────┐
   Vue3 前端 ──────▶│   Gateway    │  JWT 校验 / 限流 / 路由
                    │  (Sentinel)  │
                    └──────┬───────┘
          ┌───────────────┼────────────────┬──────────────┐
          ▼               ▼                ▼              ▼
   ┌──────────┐    ┌──────────┐    ┌──────────┐   ┌─────────────────┐
   │  user    │    │ product  │    │  order   │   │   ai-chat       │
   │ 用户/JWT │    │ 机票酒店 │    │ 订单/改签│   │  多智能体客服    │
   └────┬─────┘    └────┬─────┘    └────┬─────┘   └────┬───────┬────┘
        │               │                │  ◀─OpenFeign─┘       │
        └───────────────┴────────────────┘                     │
                        │                          ┌────────────┴───────────┐
                   PostgreSQL                       │ Router → Order/Change/ │
                                                    │ FAQ/Fallback (SAA Graph)│
        Nacos 注册中心 / 配置                        └────────────────────────┘
        Redis 短期记忆 / PostgreSQL+pgvector 向量库 + 永久历史
```

##  技术栈

| 分类 | 技术 |
|---|---|
| 微服务 | Spring Cloud Alibaba（Nacos / Gateway / Sentinel / OpenFeign / LoadBalancer） |
| AI | Spring AI Alibaba（Graph 多智能体 / Function Calling）、通义千问 qwen-max、text-embedding-v3 |
| 存储 | PostgreSQL + pgvector、Redis、MyBatis-Plus |
| 基础 | Java 17、Spring Boot 3.3、Maven 多模块 |
| 前端 | Vue3 + TypeScript + Vite + Pinia + Vue Router + Axios |
| 部署 | Docker（Nacos / PostgreSQL / Redis） |

##  模块结构

```
agent/
├── common/           # 共享：统一返回、异常处理
├── common-security/  # 共享：JWT 工具（web-neutral）
├── gateway-service/  # 网关：路由 / JWT 校验 / 限流
├── user-service/     # 用户：注册登录 / JWT 签发
├── product-service/  # 产品：机票 + 酒店
├── order-service/    # 订单：下单 / 改签（含跨服务校验）
├── ai-chat-service/  # AI 客服：多智能体 + RAG + 多轮记忆
└── frontend/         # Vue3 前端
```

##  本地运行

### 1. 启动中间件（Docker）
```bash
docker run -d --name nacos -p 8848:8848 -p 9848:9848 -e MODE=standalone -e EMBEDDED_STORAGE=embedded nacos/nacos-server:v2.4.3
docker run -d --name postgres -p 5432:5432 -e POSTGRES_USER=agent -e POSTGRES_PASSWORD=agent123 -e POSTGRES_DB=agent pgvector/pgvector:pg16
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

### 2. 初始化数据库
```bash
# 执行 db/init.sql 建表 + 种子数据
```

### 3. 配置通义千问 API Key
```bash
# 环境变量，或 IDEA Run Configuration
DASHSCOPE_API_KEY=sk-xxxxxx
```

### 4. 启动后端（依次启动）
user-service → product-service → order-service → gateway-service → ai-chat-service

### 5. 启动前端
```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

##  功能演示

> （在此插入截图：登录页、聊天界面、查订单、改签、RAG 问答、会话历史列表）

##  设计要点

- **微服务解耦**：跨服务不使用数据库外键，靠应用层（OpenFeign）保证一致性
- **改签建模**：原订单状态置为 CHANGED + 新建改签订单 + `original_order_id` 关联，保留审计链
- **对话存储冷热分离**：Redis（热，LLM 上下文，TTL）/ PostgreSQL（冷，永久历史）
- **优雅降级**：Redis 不可用时对话退化为单轮而非失败；LLM 异常触发熔断兜底
