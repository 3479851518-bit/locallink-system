# 将手动下载的 Electron zip 放入缓存或 node_modules
# 用法:
#   .\scripts\install-electron-zip.ps1 -ZipPath "D:\Downloads\electron-v28.3.3-win32-x64.zip"
#   .\scripts\install-electron-zip.ps1 -ZipPath "D:\Downloads\electron-v28.3.3-win32-x64.zip" -UseMirror

param(
    [Parameter(Mandatory = $true)]
    [string]$ZipPath,
    [switch]$UseMirror
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)

if (-not (Test-Path $ZipPath)) {
    Write-Error "找不到文件: $ZipPath"
}

$fileName = Split-Path $ZipPath -Leaf
if ($fileName -notmatch '^electron-v[\d.]+\-win32-x64\.zip$') {
    Write-Warning "文件名不像 Electron 官方包，请确认: electron-v28.3.3-win32-x64.zip"
}

# 方式一：放入 @electron/get 缓存（npm install / electron-builder 会读取）
$cacheRoot = Join-Path $env:LOCALAPPDATA "electron\Cache"
if ($UseMirror) {
    $url = "https://npmmirror.com/mirrors/electron/28.3.3/$fileName"
} else {
    $url = "https://github.com/electron/electron/releases/download/v28.3.3/$fileName"
}

$hash = node -e @"
const c=require('crypto'),u=require('url'),p=require('path');
const parsed=u.parse(process.argv[1]);
const {search,hash,pathname,...rest}=parsed;
const stripped=u.format({...rest,pathname:p.dirname(pathname||'electron')});
process.stdout.write(c.createHash('sha256').update(stripped).digest('hex'));
"@ $url

$cacheDir = Join-Path $cacheRoot $hash
$cacheZip = Join-Path $cacheDir $fileName
New-Item -ItemType Directory -Force -Path $cacheDir | Out-Null
Copy-Item -Path $ZipPath -Destination $cacheZip -Force
Write-Host "[缓存] 已复制到:"
Write-Host "  $cacheZip"

# 方式二：直接解压到 node_modules/electron/dist（最省事）
$distDir = Join-Path $root "node_modules\electron\dist"
if (Test-Path $distDir) {
    Remove-Item -Recurse -Force $distDir
}
New-Item -ItemType Directory -Force -Path $distDir | Out-Null

Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::ExtractToDirectory($ZipPath, $distDir)

$pathTxt = Join-Path $root "node_modules\electron\path.txt"
Set-Content -Path $pathTxt -Value "electron.exe" -NoNewline -Encoding ascii

Write-Host "[解压] 已解压到 node_modules\electron\dist"
Write-Host "完成。可执行: npm run electron:build-win"
