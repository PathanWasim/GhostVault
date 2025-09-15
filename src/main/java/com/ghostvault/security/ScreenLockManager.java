package com.ghostvault.security;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages screen locking functionality for enhanced security
 * Provides automatic and manual screen locking with password verification
 */
public class ScreenLockManager {
    
    private Stage lockStage;
    private PasswordField passwordField;
    private Label statusLabel;
    private Label attemptsLabel;
    private Button unlockButton;
    
    private final PasswordManager passwordManager;
    private final SessionManager sessionManager;
    private final AtomicInteger unlockAttempts;
    
    private boolean screenLocked;
    private LocalDateTime lockTime;
    private Stage parentStage;
    
    // Security settings
    private static final int MAX_UNLOCK_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 5;
    
    public ScreenLockManager(PasswordManager passwordManager, SessionManager sessionManager) {
        this.passwordManager = passwordManager;
        this.sessionManager = sessionManager;
        this.unlockAttempts = new AtomicInteger(0);
        this.screenLocked = false;
    }
    
    /**
     * Lock the screen manually
     */
    public void lockScreen(Stage parentStage) {
        if (screenLocked) {
            return; // Already locked
        }
        
        this.parentStage = parentStage;
        this.screenLocked = true;
        this.lockTime = LocalDateTime.now();
        this.unlockAttempts.set(0);
        
        Platform.runLater(() -> {
            showLockScreen();
        });
        
        logLockEvent("Screen manually locked");
    }
    
    /**
     * Lock screen automatically (due to inactivity)
     */
    public void autoLockScreen(Stage parentStage) {
        if (screenLocked) {
            return; // Already locked
        }
        
        this.parentStage = parentStage;
        this.screenLocked = true;
        this.lockTime = LocalDateTime.now();
        this.unlockAttempts.set(0);
        
        Platform.runLater(() -> {
            showLockScreen();
        });
        
        logLockEvent("Screen automatically locked due to inactivity");
    }
    
    /**
     * Show the lock screen dialog
     */
    private void showLockScreen() {
        lockStage = new Stage();
        lockStage.initModality(Modality.APPLICATION_MODAL);
        lockStage.initOwner(parentStage);
        lockStage.initStyle(StageStyle.UNDECORATED);
        lockStage.setTitle("GhostVault - Screen Locked");
        lockStage.setResizable(false);
        lockStage.setAlwaysOnTop(true);
        
        // Make it fullscreen to prevent bypassing
        lockStage.setFullScreen(true);
        lockStage.setFullScreenExitHint(\"\");
        
        VBox root = createLockScreenUI();
        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        
        // Prevent Alt+Tab and other shortcuts
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ALT || 
                event.getCode() == KeyCode.TAB ||
                event.getCode() == KeyCode.WINDOWS ||
                event.getCode() == KeyCode.ESCAPE) {
                event.consume();
            }
        });
        
        lockStage.setScene(scene);
        lockStage.show();
        
        // Focus on password field
        Platform.runLater(() -> passwordField.requestFocus());
    }
    
    /**
     * Create the lock screen UI
     */
    private VBox createLockScreenUI() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle(\"-fx-background-color: #1a1a1a;\");
        
        // Lock icon (using text for simplicity)
        Label lockIcon = new Label(\"🔒\");
        lockIcon.setFont(Font.font(\"System\", 72));
        lockIcon.setTextFill(Color.WHITE);
        
        // Title
        Label titleLabel = new Label(\"GhostVault Locked\");
        titleLabel.setFont(Font.font(\"System\", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        
        // Lock time info
        Label lockTimeLabel = new Label(\"Locked at: \" + lockTime.format(java.time.format.DateTimeFormatter.ofPattern(\"HH:mm:ss\")));
        lockTimeLabel.setFont(Font.font(\"System\", 14));
        lockTimeLabel.setTextFill(Color.LIGHTGRAY);
        
        // Instructions
        Label instructionsLabel = new Label(\"Enter your master password to unlock\");
        instructionsLabel.setFont(Font.font(\"System\", 16));
        instructionsLabel.setTextFill(Color.LIGHTGRAY);
        
        // Password input
        VBox passwordSection = createPasswordSection();
        
        // Status and attempts
        VBox statusSection = createStatusSection();
        
        // Emergency options
        VBox emergencySection = createEmergencySection();
        
        root.getChildren().addAll(
            lockIcon,
            titleLabel,
            lockTimeLabel,
            new Region(), // Spacer
            instructionsLabel,
            passwordSection,
            statusSection,
            new Region(), // Spacer
            emergencySection
        );
        
        return root;
    }
    
    /**
     * Create password input section
     */
    private VBox createPasswordSection() {
        VBox section = new VBox(15);
        section.setAlignment(Pos.CENTER);
        section.setMaxWidth(400);
        
        passwordField = new PasswordField();
        passwordField.setPromptText(\"Master Password\");
        passwordField.setPrefWidth(300);
        passwordField.setPrefHeight(40);
        passwordField.setStyle(\n            \"-fx-font-size: 16px; \" +\n            \"-fx-background-color: #333333; \" +\n            \"-fx-text-fill: white; \" +\n            \"-fx-prompt-text-fill: #888888;\"\n        );\n        \n        // Handle Enter key\n        passwordField.setOnKeyPressed(event -> {\n            if (event.getCode() == KeyCode.ENTER) {\n                attemptUnlock();\n            }\n        });\n        \n        unlockButton = new Button(\"Unlock\");\n        unlockButton.setPrefWidth(150);\n        unlockButton.setPrefHeight(40);\n        unlockButton.setStyle(\n            \"-fx-font-size: 16px; \" +\n            \"-fx-background-color: #4CAF50; \" +\n            \"-fx-text-fill: white;\"\n        );\n        unlockButton.setOnAction(e -> attemptUnlock());\n        \n        section.getChildren().addAll(passwordField, unlockButton);\n        \n        return section;\n    }\n    \n    /**\n     * Create status section\n     */\n    private VBox createStatusSection() {\n        VBox section = new VBox(10);\n        section.setAlignment(Pos.CENTER);\n        \n        statusLabel = new Label(\"\");\n        statusLabel.setFont(Font.font(\"System\", 14));\n        statusLabel.setTextFill(Color.RED);\n        \n        attemptsLabel = new Label(\"\");\n        attemptsLabel.setFont(Font.font(\"System\", 12));\n        attemptsLabel.setTextFill(Color.ORANGE);\n        \n        section.getChildren().addAll(statusLabel, attemptsLabel);\n        \n        return section;\n    }\n    \n    /**\n     * Create emergency section\n     */\n    private VBox createEmergencySection() {\n        VBox section = new VBox(10);\n        section.setAlignment(Pos.CENTER);\n        \n        Label emergencyLabel = new Label(\"Emergency Options\");\n        emergencyLabel.setFont(Font.font(\"System\", FontWeight.BOLD, 14));\n        emergencyLabel.setTextFill(Color.YELLOW);\n        \n        Button panicButton = new Button(\"Panic Mode (Destroy All Data)\");\n        panicButton.setPrefWidth(250);\n        panicButton.setStyle(\n            \"-fx-background-color: #f44336; \" +\n            \"-fx-text-fill: white; \" +\n            \"-fx-font-weight: bold;\"\n        );\n        panicButton.setOnAction(e -> handlePanicMode());\n        \n        Label warningLabel = new Label(\"⚠️ Panic mode will permanently destroy all vault data\");\n        warningLabel.setFont(Font.font(\"System\", 10));\n        warningLabel.setTextFill(Color.RED);\n        warningLabel.setWrapText(true);\n        warningLabel.setMaxWidth(300);\n        \n        section.getChildren().addAll(emergencyLabel, panicButton, warningLabel);\n        \n        return section;\n    }\n    \n    /**\n     * Attempt to unlock the screen\n     */\n    private void attemptUnlock() {\n        String password = passwordField.getText();\n        \n        if (password.isEmpty()) {\n            showStatus(\"Please enter your password\", Color.ORANGE);\n            return;\n        }\n        \n        try {\n            PasswordManager.PasswordType passwordType = passwordManager.validatePassword(password);\n            \n            switch (passwordType) {\n                case MASTER:\n                    unlockScreen();\n                    logLockEvent(\"Screen unlocked with master password\");\n                    break;\n                    \n                case PANIC:\n                    handlePanicMode();\n                    break;\n                    \n                case DECOY:\n                    // For security, treat decoy password as invalid at lock screen\n                    handleInvalidPassword();\n                    break;\n                    \n                case INVALID:\n                default:\n                    handleInvalidPassword();\n                    break;\n            }\n            \n        } catch (Exception e) {\n            showStatus(\"Authentication error: \" + e.getMessage(), Color.RED);\n            handleInvalidPassword();\n        }\n        \n        // Clear password field\n        passwordField.clear();\n    }\n    \n    /**\n     * Handle invalid password attempt\n     */\n    private void handleInvalidPassword() {\n        int attempts = unlockAttempts.incrementAndGet();\n        \n        showStatus(\"Invalid password\", Color.RED);\n        updateAttemptsLabel(attempts);\n        \n        logLockEvent(\"Invalid unlock attempt #\" + attempts);\n        \n        // Record failed login for security monitoring\n        if (sessionManager != null) {\n            sessionManager.recordFailedLogin(\"screen-unlock\", \"local\");\n        }\n        \n        if (attempts >= MAX_UNLOCK_ATTEMPTS) {\n            handleMaxAttemptsReached();\n        }\n    }\n    \n    /**\n     * Handle maximum unlock attempts reached\n     */\n    private void handleMaxAttemptsReached() {\n        showStatus(\"Too many failed attempts. Locked for \" + LOCKOUT_DURATION_MINUTES + \" minutes.\", Color.RED);\n        \n        unlockButton.setDisable(true);\n        passwordField.setDisable(true);\n        \n        logLockEvent(\"Maximum unlock attempts reached - entering lockout period\");\n        \n        // Start lockout timer\n        java.util.Timer lockoutTimer = new java.util.Timer();\n        lockoutTimer.schedule(new java.util.TimerTask() {\n            @Override\n            public void run() {\n                Platform.runLater(() -> {\n                    unlockButton.setDisable(false);\n                    passwordField.setDisable(false);\n                    unlockAttempts.set(0);\n                    showStatus(\"Lockout period ended. You may try again.\", Color.ORANGE);\n                    updateAttemptsLabel(0);\n                });\n            }\n        }, LOCKOUT_DURATION_MINUTES * 60 * 1000);\n    }\n    \n    /**\n     * Handle panic mode activation\n     */\n    private void handlePanicMode() {\n        // Show confirmation dialog\n        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);\n        confirmAlert.setTitle(\"Panic Mode Confirmation\");\n        confirmAlert.setHeaderText(\"DESTROY ALL VAULT DATA?\");\n        confirmAlert.setContentText(\"This action cannot be undone. All files and data will be permanently destroyed.\");\n        \n        confirmAlert.showAndWait().ifPresent(response -> {\n            if (response == ButtonType.OK) {\n                try {\n                    // Execute panic mode\n                    PanicModeExecutor panicExecutor = new PanicModeExecutor();\n                    panicExecutor.executePanicMode();\n                    \n                    logLockEvent(\"PANIC MODE ACTIVATED from lock screen\");\n                    \n                    // Close application\n                    Platform.exit();\n                    System.exit(0);\n                    \n                } catch (Exception e) {\n                    showStatus(\"Panic mode failed: \" + e.getMessage(), Color.RED);\n                }\n            }\n        });\n    }\n    \n    /**\n     * Unlock the screen\n     */\n    private void unlockScreen() {\n        screenLocked = false;\n        \n        if (lockStage != null) {\n            lockStage.close();\n        }\n        \n        // Reset session activity\n        if (sessionManager != null) {\n            sessionManager.recordActivity();\n        }\n    }\n    \n    /**\n     * Show status message\n     */\n    private void showStatus(String message, Color color) {\n        Platform.runLater(() -> {\n            statusLabel.setText(message);\n            statusLabel.setTextFill(color);\n        });\n    }\n    \n    /**\n     * Update attempts label\n     */\n    private void updateAttemptsLabel(int attempts) {\n        Platform.runLater(() -> {\n            if (attempts > 0) {\n                int remaining = MAX_UNLOCK_ATTEMPTS - attempts;\n                attemptsLabel.setText(\"Failed attempts: \" + attempts + \"/\" + MAX_UNLOCK_ATTEMPTS + \n                                    \" (\" + remaining + \" remaining)\");\n            } else {\n                attemptsLabel.setText(\"\");\n            }\n        });\n    }\n    \n    /**\n     * Check if screen is locked\n     */\n    public boolean isScreenLocked() {\n        return screenLocked;\n    }\n    \n    /**\n     * Get lock duration in minutes\n     */\n    public long getLockDurationMinutes() {\n        if (lockTime == null) {\n            return 0;\n        }\n        \n        return java.time.Duration.between(lockTime, LocalDateTime.now()).toMinutes();\n    }\n    \n    /**\n     * Get unlock attempts count\n     */\n    public int getUnlockAttempts() {\n        return unlockAttempts.get();\n    }\n    \n    /**\n     * Force unlock (for emergency or testing)\n     */\n    public void forceUnlock() {\n        unlockScreen();\n        logLockEvent(\"Screen force unlocked\");\n    }\n    \n    /**\n     * Log lock-related events\n     */\n    private void logLockEvent(String event) {\n        System.out.println(\"[SCREEN-LOCK] \" + LocalDateTime.now() + \": \" + event);\n    }\n    \n    /**\n     * Get lock statistics\n     */\n    public LockStats getLockStats() {\n        return new LockStats(\n            screenLocked,\n            lockTime,\n            getLockDurationMinutes(),\n            unlockAttempts.get()\n        );\n    }\n    \n    /**\n     * Lock statistics data class\n     */\n    public static class LockStats {\n        private final boolean locked;\n        private final LocalDateTime lockTime;\n        private final long durationMinutes;\n        private final int unlockAttempts;\n        \n        public LockStats(boolean locked, LocalDateTime lockTime, long durationMinutes, int unlockAttempts) {\n            this.locked = locked;\n            this.lockTime = lockTime;\n            this.durationMinutes = durationMinutes;\n            this.unlockAttempts = unlockAttempts;\n        }\n        \n        public boolean isLocked() { return locked; }\n        public LocalDateTime getLockTime() { return lockTime; }\n        public long getDurationMinutes() { return durationMinutes; }\n        public int getUnlockAttempts() { return unlockAttempts; }\n        \n        @Override\n        public String toString() {\n            return String.format(\"LockStats{locked=%s, time=%s, duration=%dm, attempts=%d}\", \n                locked, lockTime, durationMinutes, unlockAttempts);\n        }\n    }\n}"