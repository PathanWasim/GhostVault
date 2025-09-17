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
     * Set the parent stage for the lock screen
     */
    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }
    
    /**
     * Lock the screen immediately
     */
    public void lockScreen() {
        if (screenLocked) {
            return; // Already locked
        }
        
        screenLocked = true;
        lockTime = LocalDateTime.now();
        unlockAttempts.set(0);
        
        createLockScreen();
        
        // Pause session timer while locked
        if (sessionManager != null) {
            sessionManager.pauseSession();
        }
        
        System.out.println("🔒 Screen locked at " + lockTime);
    }
    
    /**
     * Unlock the screen with password verification
     */
    public void unlockScreen(String password) {
        if (!screenLocked) {
            return; // Not locked
        }
        
        try {
            // Verify password
            PasswordManager.PasswordType passwordType = passwordManager.validatePassword(password);
            
            if (passwordType == PasswordManager.PasswordType.MASTER) {
            // Successful unlock
            screenLocked = false;
            unlockAttempts.set(0);
            
            if (lockStage != null) {
                lockStage.close();
                lockStage = null;
            }
            
            // Resume session timer
            if (sessionManager != null) {
                sessionManager.resumeSession();
            }
            
                System.out.println("🔓 Screen unlocked successfully");
                
            } else {
                // Failed unlock attempt
                int attempts = unlockAttempts.incrementAndGet();
                
                if (attempts >= MAX_UNLOCK_ATTEMPTS) {
                    // Too many failed attempts - trigger security response
                    handleMaxAttemptsReached();
                } else {
                    // Show error and remaining attempts
                    Platform.runLater(() -> {
                        statusLabel.setText("Invalid password. Please try again.");
                        attemptsLabel.setText("Attempts remaining: " + (MAX_UNLOCK_ATTEMPTS - attempts));
                        passwordField.clear();
                    });
                }
            }
        } catch (Exception e) {
            // Handle validation error
            Platform.runLater(() -> {
                statusLabel.setText("Error validating password. Please try again.");
                passwordField.clear();
            });
        }
    }
    
    /**
     * Create the lock screen interface
     */
    private void createLockScreen() {
        Platform.runLater(() -> {
            lockStage = new Stage();
            lockStage.initStyle(StageStyle.UNDECORATED);
            lockStage.initModality(Modality.APPLICATION_MODAL);
            lockStage.setResizable(false);
            lockStage.setAlwaysOnTop(true);
            
            // Make it fullscreen to prevent bypassing
            lockStage.setFullScreen(true);
            lockStage.setFullScreenExitHint("");
            
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
        });
    }
    
    /**
     * Create the lock screen UI
     */
    private VBox createLockScreenUI() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        // Lock icon (using text for simplicity)
        Label lockIcon = new Label("🔒");
        lockIcon.setFont(Font.font("System", 72));
        lockIcon.setTextFill(Color.WHITE);
        
        // Title
        Label titleLabel = new Label("GhostVault Locked");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        
        // Lock time info
        Label lockTimeLabel = new Label("Locked at: " + lockTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        lockTimeLabel.setFont(Font.font("System", 14));
        lockTimeLabel.setTextFill(Color.LIGHTGRAY);
        
        // Password input section
        VBox inputSection = new VBox(15);
        inputSection.setAlignment(Pos.CENTER);
        inputSection.setMaxWidth(400);
        
        Label instructionLabel = new Label("Enter your master password to unlock:");
        instructionLabel.setFont(Font.font("System", 16));
        instructionLabel.setTextFill(Color.WHITE);
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Master Password");
        passwordField.setPrefWidth(300);
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        
        unlockButton = new Button("Unlock");
        unlockButton.setPrefWidth(120);
        unlockButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
        
        statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);
        statusLabel.setFont(Font.font("System", 12));
        
        attemptsLabel = new Label("Attempts remaining: " + MAX_UNLOCK_ATTEMPTS);
        attemptsLabel.setTextFill(Color.YELLOW);
        attemptsLabel.setFont(Font.font("System", 12));
        
        inputSection.getChildren().addAll(
            instructionLabel, passwordField, unlockButton, statusLabel, attemptsLabel
        );
        
        // Event handlers
        unlockButton.setOnAction(e -> unlockScreen(passwordField.getText()));
        passwordField.setOnAction(e -> unlockButton.fire());
        
        root.getChildren().addAll(lockIcon, titleLabel, lockTimeLabel, inputSection);
        
        return root;
    }
    
    /**
     * Handle maximum unlock attempts reached
     */
    private void handleMaxAttemptsReached() {
        Platform.runLater(() -> {
            statusLabel.setText("Maximum attempts reached. Security lockout activated.");
            attemptsLabel.setText("System will remain locked for " + LOCKOUT_DURATION_MINUTES + " minutes.");
            
            passwordField.setDisable(true);
            unlockButton.setDisable(true);
        });
        
        // TODO: Implement lockout timer and additional security measures
        System.out.println("⚠️ Maximum unlock attempts reached - security lockout activated");
    }
    
    /**
     * Check if screen is currently locked
     */
    public boolean isScreenLocked() {
        return screenLocked;
    }
    
    /**
     * Get the time when screen was locked
     */
    public LocalDateTime getLockTime() {
        return lockTime;
    }
    
    /**
     * Get current unlock attempts count
     */
    public int getUnlockAttempts() {
        return unlockAttempts.get();
    }
    
    /**
     * Force unlock (for emergency situations)
     */
    public void forceUnlock() {
        screenLocked = false;
        unlockAttempts.set(0);
        
        if (lockStage != null) {
            Platform.runLater(() -> {
                lockStage.close();
                lockStage = null;
            });
        }
        
        if (sessionManager != null) {
            sessionManager.resumeSession();
        }
        
        System.out.println("🔓 Screen force unlocked");
    }
}