# LocalLink System Startup Script

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   LocalLink - LAN IM System" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[Info] Starting system..." -ForegroundColor Yellow
Write-Host ""

# Step 1: Start Backend
Write-Host "[1/3] Starting backend service..." -ForegroundColor Green
$backendDir = "D:\Develop\JavaSECode\locallink-system\locallink-system"
Set-Location $backendDir
$backendJob = Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$backendDir'; Write-Host 'Backend starting...' -ForegroundColor Yellow; mvn spring-boot:run" -PassThru

# Step 2: Wait for backend to be ready before starting frontend
Write-Host "[2/3] Waiting for backend to be ready..." -ForegroundColor Green
$backendReady = $false
$maxAttempts = 60  # Increased timeout for more reliability
$attempt = 0
while (-not $backendReady -and $attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
            Write-Host "OK Backend is ready!" -ForegroundColor Green
        }
    }
    catch {
        $attempt++
        Start-Sleep -Seconds 2
        if ($attempt % 5 -eq 0) {  # Show progress every 10 seconds
            Write-Host "." -NoNewline
        }
    }
}

if (-not $backendReady) {
    Write-Host ""
    Write-Host "ERROR: Backend startup timeout after waiting for $(($maxAttempts * 2)) seconds" -ForegroundColor Red
    Write-Host "Please check if there are any issues with the backend application." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Press any key to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

# Step 3: Start Frontend only after backend is confirmed ready
Write-Host ""
Write-Host "[3/3] Starting frontend service (backend is ready)..." -ForegroundColor Green
$frontendDir = "D:\Develop\JavaSECode\locallink-system\locallink-system\locallink-web"
Set-Location $frontendDir
$frontendJob = Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$frontendDir'; Write-Host 'Frontend starting...' -ForegroundColor Yellow; npm run dev" -PassThru

# Show completion
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   System Started!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host " Access URLs:" -ForegroundColor Yellow
Write-Host "   Frontend: http://localhost:3000" -ForegroundColor White
Write-Host "   Backend:  http://localhost:8080" -ForegroundColor White
Write-Host ""
Write-Host " Tips:" -ForegroundColor Yellow
Write-Host "   - Services run in separate windows" -ForegroundColor White
Write-Host "   - Close windows to stop services" -ForegroundColor White
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $backendDir
Write-Host "Press any key to close..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
