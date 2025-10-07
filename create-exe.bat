@echo off
echo ========================================
echo    GhostVault Native EXE Creator
echo      (Advanced - Optional)
echo ========================================
echo.
echo NOTE: Creating native .exe files is OPTIONAL and complex.
echo The JAR file in the 'dist' folder is recommended for most users.
echo.
echo REQUIREMENTS for .exe creation:
echo 1. Java 17+ with jpackage
echo 2. WiX Toolset v3.11+ (for Windows installers)
echo 3. Visual Studio Build Tools (for some scenarios)
echo.

REM Check if Java 17+ with jpackage is available
jpackage --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: jpackage is not available
    echo.
    echo SOLUTIONS:
    echo 1. Install Java 17+ from: https://adoptium.net/
    echo 2. Make sure jpackage is included (it should be in Java 17+)
    echo.
    echo ALTERNATIVE: Use the JAR file approach instead
    echo - It's simpler and works on all platforms
    echo - Just share the 'dist' folder with friends
    echo - They need Java 17+ installed to run it
    echo.
    pause
    exit /b 1
)

echo jpackage is available. Checking for WiX Toolset...
echo.

REM Check if WiX is available (required for Windows .exe)
where candle >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: WiX Toolset not found in PATH
    echo.
    echo WiX Toolset is required for Windows .exe creation.
    echo Download from: https://wixtoolset.org/releases/
    echo.
    echo ALTERNATIVES:
    echo 1. Use Launch4j (simpler): http://launch4j.sourceforge.net/
    echo 2. Use the JAR file approach (recommended)
    echo.
    set /p choice="Continue anyway? (y/N): "
    if /i not "%choice%"=="y" (
        echo.
        echo Cancelled. Use the JAR file in 'dist' folder instead.
        pause
        exit /b 1
    )
)

echo Step 1: Building JAR file...
call build-executable.bat

if %errorlevel% neq 0 (
    echo ERROR: Failed to build JAR file
    pause
    exit /b 1
)

echo.
echo Step 2: Attempting to create native Windows executable...
echo This may take several minutes and might fail...
echo.

jpackage ^
    --input dist ^
    --name GhostVault ^
    --main-jar GhostVault.jar ^
    --type exe ^
    --dest native ^
    --win-console ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --app-version 1.0.0 ^
    --description "GhostVault - Secure File Encryption System" ^
    --vendor "GhostVault Team" ^
    --copyright "Copyright 2024 GhostVault"

if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo        EXE CREATION FAILED
    echo ========================================
    echo.
    echo This is common and expected. Possible reasons:
    echo - WiX Toolset not properly installed
    echo - JavaFX modules not properly configured
    echo - System permissions issues
    echo - Missing Visual Studio Build Tools
    echo.
    echo RECOMMENDED SOLUTION:
    echo Use the JAR file approach instead:
    echo 1. Share the 'dist' folder with friends
    echo 2. They install Java 17+ from https://adoptium.net/
    echo 3. They run GhostVault.bat to start the app
    echo.
    echo This is actually BETTER because:
    echo - Smaller file size (~50MB vs ~200MB+)
    echo - Works on Windows, Mac, and Linux
    echo - Easier to update and maintain
    echo - No complex build tools required
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo        NATIVE EXE CREATED!
echo ========================================
echo.
echo Your native Windows executable is ready:
echo   native\GhostVault-1.0.0.exe
echo.
echo This .exe file includes:
echo - Java runtime (no Java installation needed)
echo - All dependencies bundled
echo - Windows installer
echo - Desktop shortcut
echo - Start menu entry
echo.
echo File size: ~200MB+ (much larger than JAR)
echo.
echo You can now share this .exe file with your friends!
echo They don't need Java installed to run it.
echo.
echo NOTE: The JAR approach is still recommended for most users
echo as it's smaller, cross-platform, and easier to distribute.
echo.
pause