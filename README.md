# LocalLink - 局域网即时通讯系统

## 简介
基于 Spring Boot + Vue 3 的局域网即时通讯系统，支持私聊、群聊、文件传输等功能，提供 Web 版和桌面客户端。

## 技术栈
- **后端**: Spring Boot 3.2.3, MyBatis-Plus, MySQL 8.0, WebSocket, RabbitMQ, JDK 17
- **前端**: Vue 3, Vite 5, Element Plus, Pinia, Electron

## 快速开始

### 1. 初始化数据库
执行 `src/main/resources/init.sql` 创建数据库和表

### 2. 修改配置
在 `application.yml` 中配置数据库和 RabbitMQ

### 3. 启动后端
```bash
mvn clean package -DskipTests
java -jar target/locallink-system-0.0.1-SNAPSHOT.jar
```

### 4. 启动前端
```bash
cd locallink-web
npm install
npm run dev
```

访问: http://localhost:3000

### 5. 桌面客户端（可选）
```bash
cd locallink-web
npm run electron:build-win
```

## 核心功能
- ✅ 用户注册/登录、在线状态管理
- ✅ 好友系统（添加/删除/拉黑/备注）
- ✅ 群组管理（创建/加入/权限控制）
- ✅ 实时通讯（私聊/群聊/消息撤回）
- ✅ 文件传输（头像上传/文件发送）
- ✅ 消息通知（系统通知/好友申请/处罚通知）
- ✅ 管理员控制台（用户管理/举报处理/申诉处理）

## 默认账号
管理员: `admin` / `123456`

## 项目结构
```
locallink-system/
├── src/main/java/com/locallink/  # 后端代码
│   ├── controller/               # 控制器
│   ├── service/                  # 业务逻辑
│   ├── entity/                   # 实体类
│   ├── mapper/                   # 数据访问
│   ├── websocket/                # WebSocket处理
│   └── enums/                    # 枚举类
├── src/main/resources/           # 配置文件
├── locallink-web/                # 前端代码
│   ├── src/
│   │   ├── api/                  # API接口
│   │   ├── views/                # 页面组件
│   │   ├── components/           # 公共组件
│   │   ├── store/                # 状态管理
│   │   ├── utils/                # 工具类
│   │   ├── constants/            # 常量枚举
│   │   └── router/               # 路由配置
│   └── electron/                 # 桌面客户端
└── uploads/                      # 文件上传目录
```

## 文档
- [API文档](API文档.md) - 接口说明
- [用户使用手册](用户使用手册.md) - 功能指南

---
**版本**: v1.0.0
