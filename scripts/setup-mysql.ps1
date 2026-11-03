# 初始化 MySQL 并生成本地配置
# 用法: .\scripts\setup-mysql.ps1 -Password "你的MySQL密码"

param(
    [Parameter(Mandatory = $true)]
    [string]$Password
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$sqlFile = Join-Path $root "locallink.sql"
$localYml = Join-Path $root "application-local.yml"

if (-not (Test-Path $sqlFile)) {
    Write-Error "未找到 locallink.sql"
}

& mysql -uroot "-p$Password" -e "SELECT 1" | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Error "MySQL 连接失败，请检查密码或服务是否已启动"
}

Write-Host "正在导入数据库..."
Get-Content $sqlFile -Raw -Encoding UTF8 | & mysql -uroot "-p$Password"
if ($LASTEXITCODE -ne 0) {
    Write-Error "导入 locallink.sql 失败"
}

@"
spring:
  datasource:
    password: $Password
"@ | Set-Content -Path $localYml -Encoding UTF8

Write-Host "完成。请使用 MySQL 模式启动:"
Write-Host "  mvn spring-boot:run -Dspring-boot.run.profiles=mysql"
