@echo off
REM GhostVault Windows Build Script
:MENU
cls
ECHO =============================
ECHO   GhostVault Build Menu
ECHO =============================
ECHO 1. Clean
ECHO 2. Compile
ECHO 3. Run
ECHO 4. Test
ECHO 5. Package
ECHO 6. Quick Start
ECHO 7. Exit
set /p option=Choose an option: 
IF "%option%"=="1" mvn clean
IF "%option%"=="2" mvn compile
IF "%option%"=="3" mvn javafx:run
IF "%option%"=="4" mvn test
IF "%option%"=="5" mvn package
IF "%option%"=="6" (mvn clean compile && mvn javafx:run)
IF "%option%"=="7" exit
PAUSE
goto MENU
