@echo off
echo ====================================================
echo           GhostVault Test Execution Script
echo ====================================================
echo.

REM Set up environment
set JAVA_HOME=%JAVA_HOME%
set CLASSPATH=.;src\main\java;src\test\java

echo 🔧 Compiling GhostVault...
echo ----------------------------------------------------

REM Create output directories
if not exist "build\classes\main" mkdir build\classes\main
if not exist "build\classes\test" mkdir build\classes\test

REM Compile main source files
echo Compiling main source files...
javac -d build\classes\main -cp "%CLASSPATH%" src\main\java\com\ghostvault\*.java src\main\java\com\ghostvault\config\*.java src\main\java\com\ghostvault\core\*.java src\main\java\com\ghostvault\model\*.java src\main\java\com\ghostvault\security\*.java src\main\java\com\ghostvault\ui\*.java src\main\java\com\ghostvault\util\*.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Main source compilation failed!
    pause
    exit /b 1
)

REM Compile test source files
echo Compiling test source files...
javac -d build\classes\test -cp "%CLASSPATH%;build\classes\main" src\test\java\com\ghostvault\*.java src\test\java\com\ghostvault\core\*.java src\test\java\com\ghostvault\security\*.java src\test\java\com\ghostvault\ui\*.java src\test\java\com\ghostvault\performance\*.java

if %ERRORLEVEL% neq 0 (
    echo ❌ Test source compilation failed!
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.

echo 🧪 Running Comprehensive Test Suite...
echo ----------------------------------------------------

REM Run the comprehensive test runner
java -cp "build\classes\main;build\classes\test" com.ghostvault.ComprehensiveTestRunner

if %ERRORLEVEL% equ 0 (
    echo.
    echo 🎉 All tests completed successfully!
    echo GhostVault is ready for use.
) else (
    echo.
    echo ❌ Some tests failed. Please review the output above.
    echo Fix any issues before using GhostVault.
)

echo.
echo Press any key to exit...
pause > nul