# Full desktop package: backend jar + Electron exe
$ErrorActionPreference = "Stop"
$webRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$repoRoot = (Get-Item $webRoot).Parent.FullName

Write-Host "[1/3] Building backend (mvn)..."
Set-Location $repoRoot
mvn clean package -DskipTests

Write-Host "[2/3] Copying jar..."
$jar = Get-ChildItem (Join-Path $repoRoot "target") -Filter "locallink-system-*.jar" | Select-Object -First 1
if (-not $jar) {
    Write-Error "Backend jar not found after mvn package"
}
$targetDir = Join-Path $webRoot "target"
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
Copy-Item $jar.FullName (Join-Path $targetDir $jar.Name) -Force

Write-Host "[3/3] Building Electron..."
Set-Location $webRoot
npm run electron:build-win

Write-Host "Done: $webRoot\dist-electron\win-unpacked\LocalLink.exe"
