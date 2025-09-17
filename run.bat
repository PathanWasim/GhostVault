@echo off
echo Starting GhostVault...
java --module-path "target/classes" --add-modules javafx.controls,javafx.fxml,javafx.media -cp "target/ghostvault-1.0.0.jar" com.ghostvault.GhostVaultApplication
pause