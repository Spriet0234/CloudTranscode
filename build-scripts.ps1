# CloudTranscode Build Scripts
# Usage: .\build-scripts.ps1 [command]

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

function Show-Help {
    Write-Host "CloudTranscode Build Scripts" -ForegroundColor Green
    Write-Host "=============================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Commands:" -ForegroundColor Yellow
    Write-Host "  dev-backend     - Fast backend build (dev profile, no heavy media deps)" -ForegroundColor White
    Write-Host "  prod-backend    - Full backend build (prod profile, all deps)" -ForegroundColor White
    Write-Host "  frontend        - Build frontend only" -ForegroundColor White
    Write-Host "  frontend-dev    - Start frontend dev server" -ForegroundColor White
    Write-Host "  all-dev         - Fast build for both backend and frontend" -ForegroundColor White
    Write-Host "  all-prod        - Full build for both backend and frontend" -ForegroundColor White
    Write-Host "  clean           - Clean all build artifacts" -ForegroundColor White
    Write-Host "  run-backend     - Run backend in dev mode" -ForegroundColor White
    Write-Host "  run-frontend    - Run frontend in dev mode" -ForegroundColor White
    Write-Host ""
    Write-Host "Examples:" -ForegroundColor Yellow
    Write-Host "  .\build-scripts.ps1 dev-backend" -ForegroundColor White
    Write-Host "  .\build-scripts.ps1 all-dev" -ForegroundColor White
}

function Build-BackendDev {
    Write-Host "Building backend (dev profile)..." -ForegroundColor Green
    Set-Location backend
    mvn clean compile -Pdev
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Backend dev build successful!" -ForegroundColor Green
    } else {
        Write-Host "Backend dev build failed!" -ForegroundColor Red
        exit 1
    }
    Set-Location ..
}

function Build-BackendProd {
    Write-Host "Building backend (prod profile)..." -ForegroundColor Green
    Set-Location backend
    mvn clean compile -Pprod
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Backend prod build successful!" -ForegroundColor Green
    } else {
        Write-Host "Backend prod build failed!" -ForegroundColor Red
        exit 1
    }
    Set-Location ..
}

function Build-Frontend {
    Write-Host "Building frontend..." -ForegroundColor Green
    Set-Location frontend
    npm run build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Frontend build successful!" -ForegroundColor Green
    } else {
        Write-Host "Frontend build failed!" -ForegroundColor Red
        exit 1
    }
    Set-Location ..
}

function Start-FrontendDev {
    Write-Host "Starting frontend dev server..." -ForegroundColor Green
    Set-Location frontend
    npm run dev
}

function Clean-All {
    Write-Host "Cleaning all build artifacts..." -ForegroundColor Green
    Set-Location backend
    mvn clean
    Set-Location ..
    Set-Location frontend
    Remove-Item -Recurse -Force dist -ErrorAction SilentlyContinue
    Remove-Item -Recurse -Force node_modules -ErrorAction SilentlyContinue
    Set-Location ..
    Write-Host "Clean complete!" -ForegroundColor Green
}

function Run-Backend {
    Write-Host "Running backend in dev mode..." -ForegroundColor Green
    Set-Location backend
    mvn spring-boot:run -Pdev
}

function Run-Frontend {
    Write-Host "Running frontend in dev mode..." -ForegroundColor Green
    Set-Location frontend
    npm run dev
}

# Main script logic
switch ($Command.ToLower()) {
    "dev-backend" { Build-BackendDev }
    "prod-backend" { Build-BackendProd }
    "frontend" { Build-Frontend }
    "frontend-dev" { Start-FrontendDev }
    "all-dev" { 
        Build-BackendDev
        Build-Frontend
    }
    "all-prod" { 
        Build-BackendProd
        Build-Frontend
    }
    "clean" { Clean-All }
    "run-backend" { Run-Backend }
    "run-frontend" { Run-Frontend }
    default { Show-Help }
} 