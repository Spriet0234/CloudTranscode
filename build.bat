@echo off
echo CloudTranscode Quick Build Script
echo ================================

if "%1"=="dev" (
    echo Building backend (dev profile)...
    cd backend
    mvn clean compile -Pdev
    cd ..
    echo Building frontend...
    cd frontend
    npm run build
    cd ..
    echo Build complete!
) else if "%1"=="prod" (
    echo Building backend (prod profile)...
    cd backend
    mvn clean compile -Pprod
    cd ..
    echo Building frontend...
    cd frontend
    npm run build
    cd ..
    echo Build complete!
) else if "%1"=="backend-dev" (
    echo Building backend (dev profile)...
    cd backend
    mvn clean compile -Pdev
    cd ..
) else if "%1"=="backend-prod" (
    echo Building backend (prod profile)...
    cd backend
    mvn clean compile -Pprod
    cd ..
) else if "%1"=="frontend" (
    echo Building frontend...
    cd frontend
    npm run build
    cd ..
) else (
    echo Usage:
    echo   build.bat dev      - Fast build (dev profile, Firebase included)
    echo   build.bat prod     - Full build (prod profile, all deps)
    echo   build.bat backend-dev  - Backend only (dev)
    echo   build.bat backend-prod - Backend only (prod)
    echo   build.bat frontend - Frontend only
) 