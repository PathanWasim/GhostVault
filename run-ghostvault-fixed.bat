@echo off
echo Starting GhostVault...
echo.

REM Check if JAR exists
if not exist "target\ghostvault-1.0.0.jar" (
    echo ERROR: JAR file not found!
    echo Please build the project first with: mvn clean install "-Dmaven.test.skip=true"
    pause
    exit /b 1
)

REM Run with JavaFX modules explicitly
java --module-path target\ghostvault-1.0.0.jar ^
     --add-modules javafx.controls,javafx.fxml,javafx.media ^
     -jar target\ghostvault-1.0.0.jar

if errorlevel 1 (
    echo.
    echo ERROR: Failed to start GhostVault
    echo.
    echo Trying alternative method with Maven...
    echo.
    mvn javafx:run
)

pause
