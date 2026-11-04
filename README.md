# LocalLink · 局域网即时通讯系统

<p>
  <img alt="JDK" src="https://img.shields.io/badge/JDK-17-orange">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen">
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.4-42b883">
  <img alt="Vite" src="https://img.shields.io/badge/Vite-5-646cff">
  <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.0-4479A1">
  <img alt="WebSocket" src="https://img.shields.io/badge/WebSocket-Jakarta-blue">
  <img alt="Electron" src="https://img.shields.io/badge/Electron-28-47848F">
  <img alt="AI Assisted" src="https://img.shields.io/badge/AI%20Assisted-Cursor-000000">
  <img alt="License" src="https://img.shields.io/badge/License-MIT-yellow">
</p>

一套开箱即用的**局域网即时通讯系统**：后端 Spring Boot 3 + 前端 Vue 3 + Jakarta WebSocket + Electron 桌面端，
支持私聊、群聊、文件传输、消息撤回、通知中心与后台管理，Web 与 Windows 桌面双端可用。

## 功能特性

- **账号体系**：注册 / 登录（BCrypt 加密）、在线状态、个人资料与头像
- **好友系统**：好友申请 / 同意 / 拒绝、备注、拉黑、删除
- **群组管理**：建群、入群审核、群主 / 管理员 / 成员三级权限、踢人、转让、解散
- **实时通讯**：私聊与群聊、文本 / 图片 / 文件 / 语音消息、2 分钟内撤回、离线补拉
- **文件传输**：头像与聊天文件上传下载、进度反馈、大小与类型校验
- **通知中心**：好友申请、系统通知、处罚通知，未读数与实时推送
- **后台管理**：用户封禁 / 禁言、角色调整、举报与申诉处理
- **桌面客户端**：Electron 打包 Windows 应用，内嵌后端 JAR，安装即用

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2.3、Spring MVC、MyBatis-Plus 3.5、Jakarta WebSocket、Spring AOP、Bean Validation |
| 数据库 | MySQL 8.0（生产）、H2（开发，零配置） |
| 前端 | Vue 3.4、Vite 5、Pinia、Vue Router 4、Element Plus 2.5、Axios |
| 桌面端 | Electron 28 + electron-builder（Windows） |
| 部署 | Nginx 反向代理（静态资源 / API / WebSocket 升级）、PowerShell 脚本 |

> 实时消息走 WebSocket 长连接（30s 心跳、自动重连），未启用消息队列。

## 系统架构

```
┌────────────────────┐        HTTP / WebSocket        ┌──────────────────────────┐
│  Web 端 / Electron  │ ─────────────────────────────▶ │  Nginx（静态 + 反向代理）  │
└────────────────────┘                                └────────────┬─────────────┘
                                                                   │ /api  /ws
                                                                   ▼
                                                      ┌──────────────────────────┐
                                                      │      Spring Boot 3       │
                                                      │  Controller / Service    │
                                                      │  WebSocket Session 管理   │
                                                      │  MyBatis-Plus / AOP      │
                                                      └────────────┬─────────────┘
                                                                   │
                                                      ┌────────────▼─────────────┐
                                                      │     MySQL 8  /  H2       │
                                                      │   8 张业务表 + 本地文件存储 │
                                                      └──────────────────────────┘
```

## 快速开始

### 前置要求

- JDK 17+、Maven 3.8+
- Node.js 18+（前端）
- MySQL 8.0（可选，开发模式用内置 H2，**无需安装数据库**）

### 方式一：零配置启动（H2，推荐先跑通）

```bash
git clone https://github.com/3479851518-bit/locallink-system.git
cd locallink-system

# 1. 启动后端（默认 dev profile，自动建表并初始化数据）
mvn spring-boot:run

# 2. 另开终端启动前端
cd locallink-web
npm install
npm run dev
```

- 前端：http://localhost:3000
- 后端：http://localhost:8080
- H2 控制台：http://localhost:8080/h2-console （JDBC URL：`jdbc:h2:file:./data/locallink`，用户名 `sa`，密码为空）

默认管理员账号：`admin` / `123456`（**上线前务必修改**）

### 方式二：使用 MySQL

```bash
# 1. 导入表结构与初始数据
mysql -u root -p < locallink.sql

# 2. 配置本机数据库密码
cp application-local.yml.example application-local.yml
# 编辑 application-local.yml，填写你的 MySQL 密码

# 3. 以 mysql profile 启动
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

也可以通过环境变量覆盖：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`。

### 桌面客户端

```bash
cd locallink-web
npm install
npm run electron:dev        # 本地调试
npm run electron:build-win  # 打包 Windows 客户端
```

## 📦 生产部署

- [PRODUCTION_DEPLOY.md](PRODUCTION_DEPLOY.md) —— 一体化部署（Nginx + JAR + 静态资源）
- [DEPLOY.md](DEPLOY.md) —— 服务器部署步骤
- [WEBSOCKET_FIX_GUIDE.md](WEBSOCKET_FIX_GUIDE.md) —— Nginx 下 WebSocket 断开问题排查
- [nginx-config-fixed.conf](nginx-config-fixed.conf) —— 可直接参考的 Nginx 配置

## 项目结构

```
locallink-system/
├── src/main/java/com/locallink/
│   ├── controller/        # REST 接口
│   ├── service/           # 业务逻辑
│   ├── mapper/            # MyBatis-Plus 数据访问
│   ├── entity/            # 实体类
│   ├── websocket/         # WebSocket 端点与会话管理
│   ├── aspect/            # 权限 / 日志切面
│   ├── config/            # Web、WebSocket、MyBatis-Plus 配置
│   └── common/            # 统一响应、异常、枚举
├── src/main/resources/
│   ├── db/                # H2 建表与初始化脚本
│   ├── mappers/           # MyBatis XML
│   └── application*.yml   # 多环境配置
├── locallink-web/         # Vue 3 前端
│   ├── src/               # 页面、组件、API、状态管理
│   └── electron/          # 桌面端主进程与打包配置
├── locallink.sql          # MySQL 建库脚本
└── uploads/               # 运行时文件存储（不纳入版本库）
```

## 文档

- [API文档.md](API文档.md) —— REST 接口与 WebSocket 消息格式
- [用户使用手册.md](用户使用手册.md) —— 功能使用说明

## 常见问题

| 问题 | 解决 |
| --- | --- |
| 生产环境 WebSocket 连上就断 | 检查 Nginx 是否转发 `Upgrade`/`Connection` 头，参考 [WEBSOCKET_FIX_GUIDE.md](WEBSOCKET_FIX_GUIDE.md) |
| 8080 端口被占用 | 修改 `server.port` 或结束占用进程 |
| H2 数据文件被锁 | 开发模式下已开启 `AUTO_SERVER=TRUE`，确认没有重复启动后端 |
| 前端请求 404 | 确认后端已启动，且 `VITE_API_BASE_URL` 指向正确地址 |

## 安全建议

- 默认管理员密码 `123456` 仅用于本地演示，部署前请修改
- 不要把 `application-local.yml`、`.env` 提交到仓库（已在 `.gitignore` 中忽略）
- 生产环境建议为数据库使用独立账号，并开启 HTTPS

## 开发工具与 AI 协助

本项目开发过程中使用 **Cursor** 作为 AI 编程助手：部分前端页面组件、后端样板代码与文档初稿由其生成，
均经人工审查、修改与联调测试后合入。

- 架构设计、业务逻辑与技术选型由作者本人完成
- 所有提交均经过人工 review，AI 生成内容不直接上线
- 提交信息中使用 `Assisted-by: Cursor` 标注由 AI 协助完成的改动，便于追溯

## 贡献

欢迎 Issue 与 PR，提交前请确保：

1. 代码可正常编译（`mvn -q -DskipTests package`）与构建（`npm run build`）
2. 不包含本地路径、账号密码等敏感信息

##  许可证

本项目基于 [MIT License](LICENSE) 开源，可自由使用、修改与分发。
