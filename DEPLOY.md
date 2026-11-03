# LocalLink 部署说明

## 📦 构建产物

### 后端
- **文件位置**: `target/locallink-system-0.0.1-SNAPSHOT.jar`
- **文件大小**: 约 45.5 MB
- **运行命令**:
  ```bash
  java -jar target/locallink-system-0.0.1-SNAPSHOT.jar
  ```

### 前端
- **文件位置**: `locallink-web/dist/`
- **包含文件**:
  - `index.html` - 主页面
  - `assets/` - 静态资源（JS、CSS等）

## 🔧 WebSocket 连接修复

本次构建已修复 WebSocket 连接失败问题：

### 修复内容
1. ✅ **前端优化** - Main.vue 在建立 WebSocket 连接前自动获取服务器配置
2. ✅ **后端优化** - UserController 支持从 Nginx 转发头获取真实主机信息
3. ✅ **WebSocket URL 构建** - 自动适配 HTTP/HTTPS 和端口号

### Nginx 配置要求

使用 `nginx-config-fixed.conf` 替换现有配置：

```bash
# 备份原配置
cp /etc/nginx/sites-available/locallink /etc/nginx/sites-available/locallink.bak

# 应用新配置
cp nginx-config-fixed.conf /etc/nginx/sites-available/locallink

# 测试配置
nginx -t

# 重载 Nginx
systemctl reload nginx
```

**关键改进**:
- 增加 `proxy_buffering off` 禁用缓冲
- 增加 `proxy_cache_bypass $http_upgrade`
- 延长 WebSocket 超时时间到 3600s
- 添加 `X-Forwarded-Proto` 头传递

## 🚀 部署步骤

### 方式一：Nginx + Java（生产环境推荐）

#### 1. 部署后端
```bash
# 将 JAR 包上传到服务器
scp target/locallink-system-0.0.1-SNAPSHOT.jar user@server:/opt/locallink/

# 启动后端
cd /opt/locallink
nohup java -jar locallink-system-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 查看日志
tail -f app.log
```

#### 2. 部署前端
```bash
# 将 dist 目录上传到 Nginx 配置的路径
scp -r locallink-web/dist/* user@server:/var/www/locallink/
```

#### 3. 配置 Nginx
```bash
# 使用修复后的配置
cp nginx-config-fixed.conf /etc/nginx/sites-available/locallink
ln -s /etc/nginx/sites-available/locallink /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```

### 方式二：快速测试

```powershell
# Windows PowerShell

# 1. 启动后端（窗口1）
java -jar target\locallink-system-0.0.1-SNAPSHOT.jar

# 2. 提供前端静态文件（窗口2）
cd locallink-web\dist
npx serve -s . -l 3000

# 3. 访问 http://localhost:3000
```

## ✅ 验证清单

部署后请验证以下功能：

- [ ] 前端页面正常加载
- [ ] 用户登录成功
- [ ] **WebSocket 连接成功**（浏览器控制台无红色错误）
- [ ] 可以发送和接收消息
- [ ] 文件上传下载正常
- [ ] 头像显示正常

## 🔍 故障排查

### WebSocket 连接失败

如果仍然出现 WebSocket 连接错误：

1. **检查 Nginx 配置**
   ```bash
   nginx -t
   systemctl status nginx
   ```

2. **检查后端日志**
   ```bash
   tail -f app.log | grep -i websocket
   ```

3. **检查浏览器控制台**
   - 打开开发者工具 (F12)
   - 查看 Console 是否有 "服务器配置已加载" 日志
   - 查看 Network 标签中 WebSocket 连接状态

4. **使用测试工具**
   - 访问 `http://your-domain/websocket-test.html`
   - 输入用户 ID 测试连接

### API 请求失败

1. 检查后端是否正常运行：
   ```bash
   curl http://localhost:8080/api/user/config
   ```

2. 检查 Nginx 代理配置：
   ```bash
   tail -f /var/log/nginx/error.log
   ```

## 📝 环境变量配置（可选）

后端支持通过环境变量配置：

```bash
# 数据库配置
export DB_URL=jdbc:mysql://localhost:3306/locallink
export DB_USERNAME=root
export DB_PASSWORD=your_password

# 服务器配置
export SERVER_HOST=your-domain.com
export SERVER_PORT=8080

# 启动应用
java -jar locallink-system-0.0.1-SNAPSHOT.jar
```

##  关键修复说明

### 问题原因
前端在页面刷新或直接访问时，`serverConfig` 可能为空，导致 WebSocket 使用默认的 `localhost:8080` 地址。

### 解决方案
在 `Main.vue` 的 `setupWebSocket` 函数中：
1. 检查 `serverConfig` 是否存在
2. 如果不存在，调用 `/api/user/config` 获取配置
3. 如果获取失败，使用当前页面地址作为默认配置
4. 确保 WebSocket URL 正确构建

### 代码位置
- `locallink-web/src/views/Main.vue` - setupWebSocket 函数
- `src/main/java/com/locallink/controller/UserController.java` - getServerConfig 方法
- `locallink-web/src/utils/websocket.js` - getWebSocketUrl 函数

## 📞 技术支持

如遇问题，请检查：
1. 后端日志：`tail -f app.log`
2. Nginx 日志：`/var/log/nginx/error.log`
3. 浏览器控制台错误信息
4. 网络请求状态（F12 > Network）
