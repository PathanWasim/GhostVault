@echo off
echo ============================================================
echo 🔒 GhostVault - Secure File Management System v2.0
echo    Complete Integrated Application
echo ============================================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Java is not installed or not in PATH
    echo Please install Java 17 or higher from: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo 🚀 Starting GhostVault Integrated Application...
echo.

REM Set classpath for development mode
set CLASSPATH=.;lib\*;target\classes;..\target\classes

REM Try different methods to run the integrated application
echo Method 1: Running with JavaFX modules from source...
java -cp %CLASSPATH% --module-path lib\javafx --add-modules javafx.controls,javafx.fxml,javafx.media com.ghostvault.GhostVault 2>nul

if %errorlevel% neq 0 (
    echo Method 1 failed. Trying Method 2: Direct class execution...
    java -cp %CLASSPATH% -Djava.awt.headless=false com.ghostvault.GhostVault 2>nul
    
    if %errorlevel% neq 0 (
        echo Method 2 failed. Trying Method 3: JAR execution...
        java -Dprism.order=sw -Dprism.text=t2k -jar GhostVault.jar 2>nul
        
        if %errorlevel% neq 0 (
            echo.
            echo ========================================
            echo        JAVAFX SETUP REQUIRED
            echo ========================================
            echo.
            echo Your Java installation doesn't include JavaFX.
            echo.
            echo SOLUTIONS:
            echo.
            echo 1. EASIEST - Install Java with JavaFX:
            echo    Download from: https://bell-sw.com/pages/downloads/
            echo    Choose "Full JDK" which includes JavaFX
            echo.
            echo 2. OR - Install JavaFX separately:
            echo    Download from: https://openjfx.io/
            echo    Extract and set JAVAFX_HOME
            echo.
            echo 3. OR - Use different Java distribution:
            echo    Try Azul Zulu FX: https://www.azul.com/downloads/
            echo.
            echo Current Java version:
            java -version
            echo.
            echo For development, you can also try:
            echo mvn javafx:run
            echo.
            pause
            exit /b 1
        )
    )
)

echo.
echo ✅ GhostVault has been closed successfully.
pause