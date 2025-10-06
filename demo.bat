@echo off
echo.
echo ========================================
echo   🚀 GhostVault Enterprise Edition
echo   Professional Security Suite Demo
echo ========================================
echo.
echo 🔧 Compiling application...
call mvn compile -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.
echo 🚀 Launching GhostVault Enterprise Edition...
echo.
echo 💡 DEMO FEATURES TO TRY:
echo    📊 Dashboard Button - Real-time security monitoring
echo    📝 Notes Button - Secure notes manager  
echo    🔑 Passwords Button - Enterprise password vault
echo    🔍 Smart Search - Try "find my documents"
echo    🎨 Professional UI - Modern enterprise design
echo.
echo 🎯 Press Ctrl+C to stop the application
echo.
call mvn javafx:run