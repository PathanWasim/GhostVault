@echo off
echo ============================================
echo     GhostVault Secure File Vault
echo ============================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

REM Try to run with embedded JavaFX first
echo Starting GhostVault...
echo.

REM Run with proper module configuration
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -cp "target\ghostvault-1.0.0.jar" com.ghostvault.Launcher 2>error.log

if %errorlevel% neq 0 (
    echo.
    echo ============================================
    echo JavaFX runtime not properly configured
    echo ============================================
    echo.
    echo Option 1: Install JavaFX separately
    echo Option 2: Use OpenJDK with JavaFX included
    echo Option 3: Run debug mode instead
    echo.
    echo Running debug mode...
    java -cp "target\ghostvault-1.0.0.jar" com.ghostvault.DebugRunner
)

pause