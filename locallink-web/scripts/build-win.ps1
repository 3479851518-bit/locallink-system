# Build Windows desktop app (Electron)
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

$env:ELECTRON_MIRROR = "https://npmmirror.com/mirrors/electron/"
$env:ELECTRON_BUILDER_BINARIES_MIRROR = "https://npmmirror.com/mirrors/electron-builder-binaries/"
$env:CSC_IDENTITY_AUTO_DISCOVERY = "false"
$env:WIN_CSC_LINK = ""
$env:CSC_LINK = ""

Get-Process -Name "LocalLink", "electron" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

$unpacked = Join-Path (Get-Location) "dist-electron\win-unpacked"
if (Test-Path $unpacked) {
    Write-Host "[1/4] Cleaning old build..."
    Remove-Item -Recurse -Force $unpacked -ErrorAction SilentlyContinue
    if (Test-Path $unpacked) {
        Write-Error "Cannot remove dist-electron\win-unpacked. Close LocalLink.exe and retry."
    }
}

$codeSignCache = Join-Path $env:LOCALAPPDATA "electron-builder\Cache\winCodeSign"
if (Test-Path $codeSignCache) {
    Remove-Item -Recurse -Force $codeSignCache -ErrorAction SilentlyContinue
}

$repoRoot = (Get-Item (Join-Path (Get-Location) "..")).FullName
$jar = Get-ChildItem (Join-Path $repoRoot "target") -Filter "locallink-system-*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($jar) {
    $targetDir = Join-Path (Get-Location) "target"
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
    Copy-Item $jar.FullName (Join-Path $targetDir $jar.Name) -Force
    Write-Host "[2/4] Backend jar copied to locallink-web\target"
} else {
    Write-Host "[2/4] WARN: No backend jar. Run in repo root: mvn clean package -DskipTests"
}

Write-Host "[3/4] Building frontend..."
npm run build

Write-Host "[4/4] Packaging Electron..."
npx electron-builder --win

Write-Host "Done."
Write-Host "Run: dist-electron\win-unpacked\LocalLink.exe"
