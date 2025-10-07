@echo off
echo ========================================
echo       GhostVault Executable Builder
echo ========================================
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

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

echo Step 1: Cleaning previous builds...
mvn clean

echo.
echo Step 2: Compiling and packaging...
mvn package

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Step 3: Creating distribution folder...
if not exist "dist" mkdir dist

echo.
echo Step 4: Copying JAR file...
copy "target\ghostvault-1.0.0-shaded.jar" "dist\GhostVault.jar"

echo.
echo Step 5: Creating launcher script...
echo @echo off > "dist\GhostVault.bat"
echo echo Starting GhostVault... >> "dist\GhostVault.bat"
echo java -jar GhostVault.jar >> "dist\GhostVault.bat"
echo pause >> "dist\GhostVault.bat"

echo.
echo Step 6: Creating README file...
echo @echo off > "dist\README.txt"
echo GhostVault v1.0.0 - Secure File Encryption System >> "dist\README.txt"

echo.
echo ========================================
echo           BUILD COMPLETE!
echo ========================================
echo.
echo Your GhostVault distribution is ready in the 'dist' folder:
echo.
echo   dist\GhostVault.jar     - Executable JAR file (with all dependencies)
echo   dist\GhostVault.bat     - Windows launcher script
echo   dist\README.txt         - User instructions and documentation
echo.
echo SHARING WITH FRIENDS:
echo 1. Copy the entire 'dist' folder to them
echo 2. They need Java 17+ installed (from https://adoptium.net/)
echo 3. They run GhostVault.bat to start the application
echo.
echo FEATURES INCLUDED:
echo • Military-grade AES-256 file encryption
echo • Secure notes and password manager
echo • AI-powered file organization
echo • Real-time security monitoring
echo • Drag & drop interface
echo.
echo The JAR file is ~50MB and includes all dependencies!
echo No additional setup required except Java 17+.
echo.
pause