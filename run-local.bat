@echo off
chcp 65001 >nul
echo.
echo ========================================
echo    LocalLink - 局域网即时通讯系统
echo ========================================
echo.

echo [提示] 正在启动系统，请稍候...
echo.

REM ========================================
REM 1. 启动后端
REM ========================================
echo [1/3] 启动后端服务...
cd /d D:\课设\locallink-system
start "后端服务 - 端口8080" /min cmd /k "mvn spring-boot:run"
timeout /t 5 /nobreak >nul

REM ========================================
REM 2. 等待后端启动（最多等待120秒）
REM ========================================
echo [2/3] 等待后端启动...
set /a attempt=0
set /a max_attempts=60
:WAIT_BACKEND
if %attempt% GEQ %max_attempts% (
    echo ERROR: 后端启动超时，请检查后端应用是否有问题 -ForegroundColor Red
    echo.
    echo 按任意键退出... -ForegroundColor Yellow
    pause >nul
    exit /b 1
)
curl -s http://localhost:8080/ >nul 2>&1
if %errorlevel%==0 (
    echo OK 后端已就绪！
) else (
    set /a attempt+=1
    timeout /t 2 /nobreak >nul
    if %attempt% LSS %max_attempts% goto WAIT_BACKEND
)

REM ========================================
REM 3. 启动前端（仅在后端确认就绪后）
REM ========================================
echo.
echo [3/3] 启动前端服务（后端已就绪）...
cd /d D:\课设\locallink-system\locallink-web
start "前端服务 - 端口3000" /min cmd /k "npm run dev"

echo.
echo ========================================
echo    系统启动完成！
echo ========================================
echo.
echo  访问地址:
echo    前端: http://localhost:3000
echo    后端: http://localhost:8080
echo.
echo  提示:
echo    - 关闭此窗口不会停止服务
echo    - 服务在单独的窗口中运行
echo    - 关闭对应窗口即可停止服务
echo.
echo ========================================
echo.

exit
