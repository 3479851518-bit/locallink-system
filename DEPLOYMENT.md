# LocalLink 系统部署说明

## 打包文件

打包完成后，在 `target` 目录下会生成以下文件：

- **LocalLink.exe** (约 43.48 MB) - Windows 可执行文件
- **locallink-system-0.0.1-SNAPSHOT.jar** - Java JAR 包

## 运行方式

### 方式一：直接运行 EXE（推荐）

双击 `LocalLink.exe` 即可启动应用。

**要求**：
- 系统已安装 JDK 17 或更高版本
- 设置 `JAVA_HOME` 环境变量

### 方式二：使用 JAR 包运行

```bash
java -jar locallink-system-0.0.1-SNAPSHOT.jar
```

**要求**：
- 系统已安装 JDK 17 或更高版本

## 配置说明

### 1. 数据库配置

编辑 `application.yml` 或创建 `application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/locallink?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 2. RabbitMQ 配置（可选）

如果需要使用消息队列功能：

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

如果不需要，可以在配置中禁用：

```yaml
websocket:
  enabled: true
```

### 3. 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

file:
  upload:
    path: ./uploads  # 文件上传路径
```

## 系统要求

### 最低配置
- **操作系统**: Windows 10/11 (64位)
- **JDK**: 17 或更高版本
- **内存**: 至少 512MB 可用内存
- **数据库**: MySQL 8.0+
- **磁盘空间**: 至少 100MB

### 推荐配置
- **操作系统**: Windows 10/11 (64位)
- **JDK**: 17 LTS
- **内存**: 2GB 或以上
- **数据库**: MySQL 8.0+
- **磁盘空间**: 500MB 或以上（包含上传文件）

## 常见问题

### 1. 启动时提示 "找不到 JRE"

**原因**: 系统未安装 JDK 或未设置 `JAVA_HOME` 环境变量

**解决方案**:
1. 下载并安装 JDK 17: https://www.oracle.com/java/technologies/downloads/
2. 设置环境变量：
   - 右键"此电脑" → "属性" → "高级系统设置" → "环境变量"
   - 新建系统变量 `JAVA_HOME`，值为 JDK 安装路径（如 `C:\Program Files\Java\jdk-17`）
   - 在 `Path` 变量中添加 `%JAVA_HOME%\bin`

### 2. 数据库连接失败

**原因**: MySQL 未启动或配置错误

**解决方案**:
1. 确保 MySQL 服务已启动
2. 检查 `application.yml` 中的数据库配置
3. 确认数据库 `locallink` 已创建

### 3. 端口被占用

**原因**: 8080 端口已被其他程序使用

**解决方案**:
修改配置文件中的端口号：

```yaml
server:
  port: 8081  # 改为其他可用端口
```

### 4. WebSocket 连接失败

**原因**: 防火墙阻止了 WebSocket 端口

**解决方案**:
1. 在防火墙中允许应用访问
2. 或者修改 WebSocket 配置端口

## 首次启动步骤

1. **安装 MySQL**
   - 下载并安装 MySQL 8.0+
   - 创建数据库：`CREATE DATABASE locallink DEFAULT CHARACTER SET utf8mb4;`

2. **配置应用**
   - 复制 `application-example.yml` 为 `application.yml`
   - 修改数据库连接信息

3. **启动应用**
   - 双击 `LocalLink.exe` 或运行 `java -jar locallink-system-0.0.1-SNAPSHOT.jar`

4. **访问系统**
   - 后端 API: http://localhost:8080
   - Swagger 文档: http://localhost:8080/swagger-ui.html
   - 前端页面: 需要先启动前端项目（见前端部署说明）

## 重新打包

如果需要重新打包，在项目根目录执行：

```bash
mvn clean package -DskipTests
```

生成的文件位于 `target` 目录。

## 技术支持

如有问题，请查看：
- 日志文件：控制台输出
- API 文档：http://localhost:8080/swagger-ui.html
- 项目文档：README.md、API文档.md、用户使用手册.md
