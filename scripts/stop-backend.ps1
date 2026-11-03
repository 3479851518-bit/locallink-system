# 停止占用 8080 的后端进程（解决端口占用 / H2 数据库文件锁定）
$connections = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if (-not $connections) {
    Write-Host "8080 端口未被占用。"
    exit 0
}

$connections | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object {
    $proc = Get-Process -Id $_ -ErrorAction SilentlyContinue
    if ($proc) {
        Stop-Process -Id $_ -Force
        Write-Host "已停止: $($proc.ProcessName) (PID $_)"
    }
}

Start-Sleep -Seconds 1
Write-Host "可重新运行: mvn spring-boot:run"
