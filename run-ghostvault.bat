@echo off
echo ========================================
echo          GhostVault Launcher
echo    Secure File Encryption System
echo ========================================
echo.


java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in Path
    echo Please install Maven from: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo Starting GhostVault...
echo.

REM Run the application
mvn javafx:run

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to start GhostVault
    echo Check the error messages above for details
    echo.
    pause
    exit /b 1
)

echo.
echo GhostVault has been closed.
pause
