# LocalLink 客户端打包指南

## 环境要求

- Node.js 16+
- JDK 17+
- Maven 3.6+

## 打包步骤

### 1. 安装依赖

```bash
cd locallink-web
npm install
```

### 2. 打包后端

在项目根目录执行：

```bash
mvn clean package
```

会在 `target` 目录生成 `locallink-system-1.0.0.jar`

### 3. 打包前端

```bash
cd locallink-web
npm run build
```

会在 `locallink-web/dist` 目录生成前端文件

### 4. 打包 Electron 客户端

```bash
cd locallink-web
npm run electron:build-win
```

会在 `locallink-web/dist-electron` 目录生成：
- `LocalLink Setup x.x.x.exe` - Windows 安装包
- `win-unpacked` - 免安装版本

## 使用说明

### 开发模式

```bash
# 1. 启动后端（在根目录）
java -jar target/locallink-system-*.jar

# 2. 启动前端（在 locallink-web 目录）
npm run dev

# 访问 http://localhost:3000
```

### 生产模式

#### 方式一：Web 部署
1. 打包后端和前端
2. 将 jar 文件和 dist 目录部署到服务器
3. 访问 http://服务器IP:8080

#### 方式二：桌面客户端
1. 双击 `LocalLink Setup x.x.x.exe` 安装
2. 启动应用
3. 应用会自动启动后端服务
4. 即可使用，无需单独部署

## 注意事项

1. 桌面客户端需要 Java 运行环境（JRE 17+）
2. 客户端会自动启动后端服务，无需手动操作
3. 上传文件保存在客户端安装目录的 `uploads` 文件夹
4. 关闭客户端会同时停止后端服务

## 图标配置

如需自定义应用图标：
1. 准备 256x256 的 PNG 图片
2. 转换为 .ico 格式
3. 替换 `locallink-web/electron/icon.ico`
4. 重新打包

## 故障排查

### 打包失败：无法从 GitHub 下载 Electron

国内网络常见报错：`connection was forcibly closed by the remote host`。

**解决步骤（在项目 `locallink-web` 目录）：**

```powershell
# 1. 已配置 .npmrc 国内镜像，设置环境变量后重装 Electron
$env:ELECTRON_MIRROR="https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR="https://npmmirror.com/mirrors/electron-builder-binaries/"
npm run electron:reinstall

# 2. 再打包
npm run electron:build-win
```

若仍失败，可**手动下载**后一键安装（推荐）：

1. 浏览器下载（任选其一）：
   - 国内镜像：https://npmmirror.com/mirrors/electron/28.3.3/electron-v28.3.3-win32-x64.zip
   - GitHub 原站：https://github.com/electron/electron/releases/download/v28.3.3/electron-v28.3.3-win32-x64.zip

2. 在 `locallink-web` 目录执行（把路径改成你下载的位置）：

```powershell
# 从镜像下载的 zip 用 -UseMirror；从 GitHub 下载则不加该参数
.\scripts\install-electron-zip.ps1 -ZipPath "D:\Downloads\electron-v28.3.3-win32-x64.zip" -UseMirror
```

3. 再打包：`npm run electron:build-win`

---

**若要自己放进缓存目录**（了解原理即可）：

| 下载来源 | 缓存目录（在其下放 zip 文件） |
|----------|------------------------------|
| npmmirror 镜像 | `%LOCALAPPDATA%\electron\Cache\d4557505e73b3a442bf4af30a751eb9bfcf5c09f3265c7b3f328083cc76c54b0\electron-v28.3.3-win32-x64.zip` |
| GitHub 原站 | `%LOCALAPPDATA%\electron\Cache\ef33c1c1407eeea89775ef92a07226a7f3301c3466e8ffe69e5ee2998f876d40\electron-v28.3.3-win32-x64.zip` |

说明：
- `%LOCALAPPDATA%` 一般是 `C:\Users\你的用户名\AppData\Local`
- **不要解压成文件夹**，缓存里放的是 **zip 原文件**
- 更简单的方式：脚本会自动解压到 `node_modules\electron\dist`，一般不必手动管缓存

### 其他打包失败
- 清理缓存：`npm cache clean --force`
- 删除 node_modules 后 `npm install`

### 客户端无法启动
- 检查是否安装 Java 17+
- 检查 `target` 目录是否有 jar 文件
- 查看控制台错误信息

### 后端无法连接
- 检查 8080 端口是否被占用
- 检查防火墙设置
- 查看后端日志
