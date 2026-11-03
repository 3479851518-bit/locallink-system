# LocalLink 生产环境部署说明

##  部署路径

> 说明：文中 `<部署目录>`、`<项目目录>` 为占位符，请替换为实际路径（Windows 如 `C:\deploy\locallink`，Linux 如 `/opt/locallink`）。

- **后端JAR**: `<部署目录>\locallink-system\target\locallink-system-0.0.1-SNAPSHOT.jar`
- **前端dist**: `<部署目录>\locallink-web\dist\`

## 🚀 部署步骤

### 方式一：集成部署（推荐）

前端静态文件由后端直接提供，只需启动一个服务。

#### 1. 部署前端文件到后端

```powershell
# 运行部署脚本
.\deploy-frontend.ps1
```

此脚本会将 `dist` 目录的所有文件复制到后端的 `static` 目录。

#### 2. 启动应用

```powershell
# 运行生产启动脚本
.\run-prod.ps1
```

#### 3. 访问应用

打开浏览器访问: `http://localhost:8080`

---

### 方式二：分离部署

使用Nginx等反向代理，前端和后端分别部署。

#### 1. 配置Nginx

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态文件
    location / {
        root /var/www/locallink/locallink-web/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # WebSocket代理
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### 2. 启动后端

```powershell
cd <部署目录>\locallink-system\target
java -jar locallink-system-0.0.1-SNAPSHOT.jar
```

#### 3. 启动Nginx

```bash
# Windows
nginx.exe

# Linux
systemctl start nginx
```

#### 4. 访问应用

打开浏览器访问: `http://localhost` (通过Nginx)

---

## 📋 脚本说明

### `deploy-frontend.ps1`
- **功能**: 将前端dist文件部署到后端static目录
- **用途**: 集成部署前使用
- **执行**: `.\deploy-frontend.ps1`

### `run-prod.ps1`
- **功能**: 启动生产环境的后端服务
- **用途**: 使用已打包的JAR文件启动
- **执行**: `.\run-prod.ps1`
- **特点**: 
  - 自动检查JAR文件是否存在
  - 等待后端服务就绪
  - 提供详细的启动信息

### `run-local.ps1` (开发环境)
- **功能**: 启动开发环境（Maven + Vite）
- **用途**: 开发调试时使用
- **执行**: `.\run-local.ps1`

---

##  常见问题

### Q1: 前端页面404
**A**: 确保已运行 `deploy-frontend.ps1` 将dist文件部署到后端static目录。

### Q2: WebSocket连接失败
**A**: 检查后端是否正常启动，端口8080是否被占用。

### Q3: 如何更新前端
**A**: 
1. 在前端目录运行 `npm run build`
2. 运行 `.\deploy-frontend.ps1` 部署新的dist文件
3. 重启后端服务

### Q4: 如何更新后端
**A**:
1. 在后端目录运行 `mvn clean package -DskipTests`
2. 将新的JAR文件复制到生产目录
3. 运行 `.\run-prod.ps1` 启动新服务

---

##  生产环境检查清单

- [ ] JAR文件已打包到生产目录
- [ ] 前端dist文件已构建
- [ ] 已运行 `deploy-frontend.ps1` 部署前端文件
- [ ] 数据库配置正确（application.yml）
- [ ] 端口8080未被占用
- [ ] 防火墙已开放相应端口
- [ ] 日志目录有写入权限

---

## 🎯 快速开始

```powershell
# 1. 构建前后端
cd <项目目录>
mvn clean package -DskipTests
cd locallink-web
npm run build

# 2. 部署前端
cd ..
.\deploy-frontend.ps1

# 3. 启动服务
.\run-prod.ps1
```

访问: http://localhost:8080

---

## 📞 技术支持

如有问题，请检查:
- 后端日志: 控制台输出
- 前端日志: 浏览器控制台 (F12)
- 数据库连接: application.yml配置
