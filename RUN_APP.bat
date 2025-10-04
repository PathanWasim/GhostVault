@echo off
echo ========================================
echo    GhostVault Launcher
echo ========================================
echo.

REM Method 1: Try Maven JavaFX plugin (Recommended)
echo [1/3] Attempting to run with Maven JavaFX plugin...
echo.
call mvn javafx:run

if errorlevel 1 (
    echo.
    echo [1/3] Failed. Trying alternative method...
    echo.
    
    REM Method 2: Try Maven exec plugin
    echo [2/3] Attempting to run with Maven exec plugin...
    echo.
    call mvn exec:java -Dexec.mainClass="com.ghostvault.GhostVault"
    
    if errorlevel 1 (
        echo.
        echo [2/3] Failed. Trying final method...
        echo.
        
        REM Method 3: Try direct JAR execution
        echo [3/3] Attempting direct JAR execution...
        echo.
        java -jar target\ghostvault-1.0.0.jar
        
        if errorlevel 1 (
            echo.
            echo ========================================
            echo    All methods failed!
            echo ========================================
            echo.
            echo Possible solutions:
            echo 1. Ensure the project is built: mvn clean install "-Dmaven.test.skip=true"
            echo 2. Check Java version: java -version (should be 17+)
            echo 3. Install JavaFX SDK separately if needed
            echo.
            echo For more help, see RUN_GHOSTVAULT.md
            echo.
        )
    )
)

echo.
pause
