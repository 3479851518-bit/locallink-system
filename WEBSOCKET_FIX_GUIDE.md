# WebSocket 连接问题诊断与修复

## 问题现象
WebSocket 连接成功建立，但立即被关闭，触发重连。

## 控制台日志
```
正在建立 WebSocket 连接: ws://ustbsystest.sylu.cc/ws/chat/7
WebSocket 已连接
WebSocket连接成功
WebSocket 已关闭          ← 问题：连接立即关闭
WebSocket连接关闭，准备重连
准备第1次重连，延迟2000ms
```

## 根本原因

### 可能原因 1：Nginx 配置未更新（最常见）
Nginx 的 WebSocket 代理配置缺少关键参数或配置错误。

### 可能原因 2：后端服务未重启
修改代码后，后端服务没有重启，仍然运行旧代码。

### 可能原因 3：Nginx 缓冲配置问题
Nginx 默认会缓冲响应，导致 WebSocket 连接被错误处理。

## 解决步骤

### 步骤 1：确认 Nginx 配置已更新

```bash
# 查看当前 Nginx 配置
cat /etc/nginx/sites-available/locallink | grep -A 20 "location /ws"
```

**必须包含以下配置**：
```nginx
location /ws {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    
    # WebSocket 特定配置（重要！）
    proxy_buffering off;              # 禁用缓冲
    proxy_cache_bypass $http_upgrade; # 绕过缓存
    
    # 超时配置
    proxy_connect_timeout 60s;
    proxy_send_timeout 3600s;
    proxy_read_timeout 3600s;
}
```

### 步骤 2：重新部署 Nginx 配置

```bash
# 1. 使用修复后的配置
cp nginx-config-fixed.conf /etc/nginx/sites-available/locallink

# 2. 测试配置是否正确
nginx -t

# 3. 如果测试通过，重载 Nginx
systemctl reload nginx

# 或者
nginx -s reload
```

### 步骤 3：重启后端服务

```bash
# Windows 环境下
# 找到 Java 进程
tasklist | findstr java

# 终止进程
taskkill /F /PID <进程ID>

# 或使用 PowerShell
Get-Process java | Stop-Process -Force

# 重新启动后端
cd D:\Develop\JavaSECode\locallink-system\locallink-system
java -jar target\locallink-system-0.0.1-SNAPSHOT.jar
```

### 步骤 4：验证部署

#### 4.1 检查后端日志
```bash
# 查看 WebSocket 相关日志
tail -f app.log | grep -i websocket
```

**正常日志应该显示**：
```
用户7上线，当前在线人数: 1
```

**异常日志会显示**：
```
WebSocket异常,用户7
java.io.IOException: ...
```

#### 4.2 检查 Nginx 错误日志
```bash
tail -f /var/log/nginx/error.log
```

**常见错误**：
- `upstream prematurely closed connection` - 后端主动关闭连接
- `recv() failed (10054: Connection reset by peer)` - 连接被重置

#### 4.3 浏览器控制台验证
打开浏览器开发者工具 (F12)：

1. **Console 标签**：
   ```
   ✓ 服务器配置已加载: {baseUrl: "http://ustbsystest.sylu.cc", wsUrl: "ws://ustbsystest.sylu.cc", ...}
   ✓ 正在建立 WebSocket 连接: ws://ustbsystest.sylu.cc/ws/chat/7
   ✓ WebSocket 已连接
   ✓ WebSocket连接成功
   ```

2. **Network 标签**：
   - 筛选 `WS` (WebSocket)
   - 找到 `chat/7` 连接
   - 状态应该是 `101 Switching Protocols`
   - Status 列显示绿色

## 快速测试工具

使用提供的 `websocket-test.html` 进行独立测试：

```bash
# 1. 将测试文件放到 Nginx 根目录
cp websocket-test.html D:/wwwroot/locallink/locallink-web/dist/

# 2. 访问测试页面
http://ustbsystest.sylu.cc/websocket-test.html

# 3. 输入用户ID（如 7），点击连接
```

## 常见问题

### Q1: 配置已更新但仍然失败？
**A**: 检查 Nginx 是否真的重载了配置
```bash
systemctl status nginx
nginx -t  # 确认配置无语法错误
```

### Q2: 后端日志显示异常？
**A**: 查看具体异常信息
```bash
tail -n 100 app.log
```
常见异常：
- 数据库连接失败
- Session 管理异常
- 广播消息时 IO 异常

### Q3: 只有部分用户连接失败？
**A**: 检查用户权限
- 用户是否被封号
- 用户状态是否正常
- 数据库中用户记录是否完整

### Q4: 连接成功但无法发送消息？
**A**: 检查消息发送逻辑
- WebSocket 状态是否为 OPEN
- 消息格式是否正确
- 后端是否收到消息

## 紧急修复命令（一键执行）

```bash
# Linux 环境
cd /opt/locallink

# 1. 更新 Nginx 配置
cp nginx-config-fixed.conf /etc/nginx/sites-available/locallink
nginx -t && systemctl reload nginx

# 2. 重启后端
pkill -f locallink-system
nohup java -jar locallink-system-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# 3. 查看日志
tail -f app.log

# Windows PowerShell
cd D:\Develop\JavaSECode\locallink-system\locallink-system

# 1. 停止后端
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

# 2. 重启后端
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target\locallink-system-0.0.1-SNAPSHOT.jar"

# 3. 重新部署前端
xcopy /E /Y locallink-web\dist\* D:\wwwroot\locallink\locallink-web\dist\

# 4. 重载 Nginx（需要管理员权限）
nginx -s reload
```

## 联系支持

如果以上步骤都无法解决问题，请提供：
1. 后端完整日志（`app.log` 最后 100 行）
2. Nginx 错误日志（`/var/log/nginx/error.log` 最后 50 行）
3. 浏览器控制台完整截图
4. Network 标签中 WebSocket 连接的详细信息
