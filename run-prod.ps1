# LocalLink System Production Startup Script
# Production mode: Backend serves frontend from static files

$ErrorActionPreference = "Continue"

# ========================================
# Configuration
# ========================================
$backendJar = "D:\wwwroot\locallink\locallink-system\target\locallink-system-0.0.1-SNAPSHOT.jar"
$frontendDist = "D:\wwwroot\locallink\locallink-web\dist"
$backendDir = Split-Path $backendJar -Parent
# Static directory next to JAR file (configured in application.yml)
$backendStatic = Join-Path $backendDir "static"

# ========================================
# Helper Functions
# ========================================
function Show-Header {
    param([string]$Title)
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "   $Title" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

function Show-Error {
    param([string]$Message)
    Write-Host ""
    Write-Host "ERROR: $Message" -ForegroundColor Red
    Write-Host ""
    Write-Host "Press any key to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

function Show-Step {
    param([string]$Step, [string]$Message)
    Write-Host "[$Step] $Message" -ForegroundColor Green
}

function Show-Info {
    param([string]$Message)
    Write-Host "[Info] $Message" -ForegroundColor Yellow
}

function Show-Success {
    param([string]$Message)
    Write-Host "OK $Message" -ForegroundColor Green
}

function Wait-ForBackend {
    param(
        [int]$MaxAttempts = 60,
        [int]$TimeoutSec = 3,
        [int]$SleepSec = 2
    )
    
    Write-Host ""
    Write-Host "Waiting for backend to be ready..." -ForegroundColor Yellow
    
    $backendReady = $false
    $attempt = 0
    
    while (-not $backendReady -and $attempt -lt $MaxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec $TimeoutSec -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                $backendReady = $true
                Show-Success "Backend is ready!"
            }
        }
        catch {
            $attempt++
            Start-Sleep -Seconds $SleepSec
            if ($attempt % 5 -eq 0) {
                Write-Host "." -NoNewline
            }
        }
    }
    
    return $backendReady
}

# ========================================
# Main Script
# ========================================
Show-Header "LocalLink - Production Mode"

# Step 0: Pre-flight checks
Show-Info "Starting production system..."
Write-Host ""

# Check if JAR file exists
if (-not (Test-Path $backendJar)) {
    Show-Error "Backend JAR file not found`nPath: $backendJar`nPlease build the project first (mvn clean package)"
}

# Check if dist directory exists
if (-not (Test-Path $frontendDist)) {
    Show-Error "Frontend dist directory not found`nPath: $frontendDist`nPlease build the frontend first (npm run build)"
}

# Step 1: Deploy frontend files to backend static directory FIRST
Show-Step "1/2" "Deploying frontend to backend static directory..."
try {
    # Create static directory if not exists
    if (-not (Test-Path $backendStatic)) {
        Write-Host "Creating static directory..." -ForegroundColor Yellow
        New-Item -Path $backendStatic -ItemType Directory -Force | Out-Null
    }

    # Clear existing static directory
    Write-Host "Clearing old static files..." -ForegroundColor Yellow
    Get-ChildItem -Path $backendStatic -Recurse | Remove-Item -Force -Recurse -ErrorAction SilentlyContinue

    # Copy dist files to static directory
    Write-Host "Copying frontend files..." -ForegroundColor Yellow
    Copy-Item -Path "$frontendDist\*" -Destination $backendStatic -Recurse -Force

    $fileCount = (Get-ChildItem -Path $backendStatic -Recurse -File).Count
    Show-Success "Deployed $fileCount files to static directory!"
}
catch {
    Show-Error "Failed to deploy frontend files`n$($_.Exception.Message)"
}

Write-Host ""

# Step 2: Start backend service (which will serve the frontend)
Show-Step "2/2" "Starting backend service..."
try {
    $backendJob = Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$backendDir'; Write-Host 'Backend starting...' -ForegroundColor Yellow; java -jar '$backendJar'" -PassThru
    Show-Success "Backend process started (PID: $($backendJob.Id))"
}
catch {
    Show-Error "Failed to start backend`n$($_.Exception.Message)"
}

# Step 3: Wait for backend to be ready
$backendReady = Wait-ForBackend -MaxAttempts 60 -TimeoutSec 3 -SleepSec 2

if (-not $backendReady) {
    Write-Host ""
    Write-Host "ERROR: APPLICATION FAILED TO START" -ForegroundColor Red
    Write-Host "Backend service failed to start within the expected time" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please check the backend window for error messages" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Common issues:" -ForegroundColor Yellow
    Write-Host "  - Database connection problems" -ForegroundColor White
    Write-Host "  - Port 8080 already in use" -ForegroundColor White
    Write-Host "  - Configuration errors" -ForegroundColor White
    Write-Host "  - Missing dependencies" -ForegroundColor White
    Write-Host ""
    Write-Host "Press any key to exit..." -ForegroundColor Yellow
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

# ========================================
# Completion
# ========================================
Write-Host ""
Show-Header "Production System Started!"

Write-Host " Deployment Info:" -ForegroundColor Yellow
Write-Host "   Backend JAR:     $backendJar" -ForegroundColor White
Write-Host "   Frontend dist:   $frontendDist" -ForegroundColor White
Write-Host "   Static files:    $backendStatic" -ForegroundColor White
Write-Host ""
Write-Host " Access URLs:" -ForegroundColor Yellow
Write-Host "   Application:     http://localhost:8080" -ForegroundColor Green
Write-Host "   Frontend served by backend automatically" -ForegroundColor White
Write-Host ""
Write-Host " Tips:" -ForegroundColor Yellow
Write-Host "   - Frontend files are deployed to backend static directory" -ForegroundColor White
Write-Host "   - Backend runs in a separate window" -ForegroundColor White
Write-Host "   - Close the backend window to stop the service" -ForegroundColor White
Write-Host ""

Show-Header "========================================"

Write-Host "Press any key to close this window..." -ForegroundColor Yellow
Write-Host "(Backend will continue running in its own window)" -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
