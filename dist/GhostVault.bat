@echo off
echo ========================================
echo          GhostVault Launcher
echo    Secure File Encryption System
echo ========================================
echo.
echo Starting GhostVault...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

REM Run GhostVault with JavaFX modules
java --module-path . --add-modules javafx.controls,javafx.fxml,javafx.media -jar GhostVault.jar

if %errorlevel% neq 0 (
    echo.
    echo If you see JavaFX errors, try running with:
    echo java -jar GhostVault.jar
    echo.
    echo Or install JavaFX separately.
)

echo.
echo GhostVault has been closed.
pause