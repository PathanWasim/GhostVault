package com.ghostvault.ui.controllers;

import com.ghostvault.ui.components.ErrorHandlingSystem;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Authentication controller with mode detection based on password patterns
 */
public class AuthenticationController {
    
    // UI Components
    private Stage primaryStage;
    private Scene scene;
    private VBox rootContainer;
    private VBox centerContainer;
    
    // Authentication form
    private Label titleLabel;
    private Label subtitleLabel;
    private PasswordField passwordField;
    private Button loginButton;
    private Button exitButton;
    private ProgressIndicator authProgress;
    private Label statusLabel;
    
    // Mode detection
    private String masterPasswordHash;
    private String panicPasswordHash;
    private String decoyPasswordHash;
    
    // Callbacks
    private Consumer<ModeController.VaultMode> onAuthenticationSuccess;
    private Runnable onAuthenticationCancelled;
    
    // State
    private boolean authenticationInProgress = false;
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    public AuthenticationController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializePasswords();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * Initialize password hashes for different modes
     */
    private void initializePasswords() {
        // In a real implementation, these would be loaded from secure storage
        // For demo purposes, using simple patterns:
        
        // Master mode: "master123" 
        masterPasswordHash = hashPassword("master123");
        
        // Panic mode: "panic911" or "emergency"
        panicPasswordHash = hashPassword("panic911");
        
        // Decoy mode: "decoy456" or any other password
        decoyPasswordHash = hashPassword("decoy456");
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        rootContainer = new VBox();
        rootContainer.getStyleClass().add("authentication-controller");
        
        centerContainer = new VBox(20);
        centerContainer.setAlignment(Pos.CENTER);
        centerContainer.getStyleClass().add("auth-center-container");
        centerContainer.setMaxWidth(400);
        
        // Title and branding
        titleLabel = new Label("🔒 GhostVault");
        titleLabel.getStyleClass().add("auth-title");
        
        subtitleLabel = new Label("Secure Digital Vault");
        subtitleLabel.getStyleClass().add("auth-subtitle");
        
        // Authentication form
        VBox formContainer = new VBox(12);
        formContainer.getStyleClass().add("auth-form-container");
        
        Label passwordLabel = new Label("Enter your vault password:");
        passwordLabel.getStyleClass().add("auth-password-label");
        
        passwordField = new PasswordField();
        passwordField.getStyleClass().add("auth-password-field");
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        
        // Buttons
        HBox buttonContainer = new HBox(12);
        buttonContainer.setAlignment(Pos.CENTER);
        
        loginButton = new Button("Unlock Vault");
        loginButton.getStyleClass().addAll("button", "primary", "auth-login-button");
        loginButton.setPrefWidth(150);
        loginButton.setDefaultButton(true);
        
        exitButton = new Button("Exit");
        exitButton.getStyleClass().addAll("button", "ghost", "auth-exit-button");
        exitButton.setPrefWidth(100);
        
        buttonContainer.getChildren().addAll(exitButton, loginButton);
        
        // Progress and status
        authProgress = new ProgressIndicator();
        authProgress.getStyleClass().add("auth-progress");
        authProgress.setPrefSize(24, 24);
        authProgress.setVisible(false);
        
        statusLabel = new Label("");
        statusLabel.getStyleClass().add("auth-status-label");
        statusLabel.setVisible(false);
        
        formContainer.getChildren().addAll(
            passwordLabel, passwordField, buttonContainer, authProgress, statusLabel
        );
        
        // Security notice
        Label securityNotice = new Label(
            "🛡️ Your data is protected with military-grade encryption\\n" +
            "Different access levels available based on authentication"
        );
        securityNotice.getStyleClass().add("auth-security-notice");
        securityNotice.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        centerContainer.getChildren().addAll(
            titleLabel, subtitleLabel, formContainer, securityNotice
        );
        
        rootContainer.getChildren().add(centerContainer);
        VBox.setVgrow(centerContainer, Priority.ALWAYS);
        
        // Create scene
        scene = new Scene(rootContainer, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/ultra-modern-theme.css").toExternalForm());
    }
    
    /**
     * Setup layout properties
     */
    private void setupLayout() {
        rootContainer.setAlignment(Pos.CENTER);
        rootContainer.setPadding(new Insets(40));
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Login button
        loginButton.setOnAction(e -> authenticate());
        
        // Exit button
        exitButton.setOnAction(e -> exitApplication());
        
        // Enter key in password field
        passwordField.setOnAction(e -> authenticate());
        
        // Password field changes
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            loginButton.setDisable(newText.trim().isEmpty());
            
            // Clear status on new input
            if (statusLabel.isVisible()) {
                statusLabel.setVisible(false);
            }
        });
        
        // Window close request
        primaryStage.setOnCloseRequest(e -> exitApplication());
    }
    
    /**
     * Perform authentication and mode detection
     */
    private void authenticate() {
        if (authenticationInProgress) {
            return;
        }
        
        String password = passwordField.getText();
        if (password.trim().isEmpty()) {
            showStatus("Please enter a password", false);
            return;
        }
        
        authenticationInProgress = true;
        showProgress("Authenticating...");
        
        // Perform authentication in background thread
        new Thread(() -> {
            try {
                // Simulate authentication delay
                Thread.sleep(1000);
                
                // Detect mode based on password
                ModeController.VaultMode detectedMode = detectMode(password);
                
                Platform.runLater(() -> {
                    if (detectedMode != null) {
                        handleAuthenticationSuccess(detectedMode, password);
                    } else {
                        handleAuthenticationFailure();
                    }
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> handleAuthenticationFailure());
            }
        }).start();
    }
    
    /**
     * Detect vault mode based on password
     */
    private ModeController.VaultMode detectMode(String password) {
        String passwordHash = hashPassword(password);
        
        // Check for master mode
        if (passwordHash.equals(masterPasswordHash)) {
            return ModeController.VaultMode.MASTER;
        }
        
        // Check for panic mode (multiple trigger passwords)
        if (passwordHash.equals(panicPasswordHash) || 
            password.toLowerCase().contains("emergency") ||
            password.toLowerCase().contains("panic") ||
            password.matches(".*911.*")) {
            return ModeController.VaultMode.PANIC;
        }
        
        // Check for decoy mode
        if (passwordHash.equals(decoyPasswordHash)) {
            return ModeController.VaultMode.DECOY;
        }
        
        // Additional decoy triggers (wrong passwords that should look normal)
        if (password.length() >= 6 && !password.equals("master123")) {
            // Any reasonable-looking password that's not master or panic
            // triggers decoy mode for plausible deniability
            return ModeController.VaultMode.DECOY;
        }
        
        return null; // Authentication failed
    }
    
    /**
     * Handle successful authentication
     */
    private void handleAuthenticationSuccess(ModeController.VaultMode mode, String password) {
        authenticationInProgress = false;
        hideProgress();
        
        // Clear password field for security
        passwordField.clear();
        
        // Reset failed attempts
        failedAttempts = 0;
        
        // Show success message briefly
        showStatus("Authentication successful - Loading " + mode.getDisplayName(), true);
        
        // Delay before switching to give user feedback
        Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> {
                        if (onAuthenticationSuccess != null) {
                            onAuthenticationSuccess.accept(mode);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        });
    }
    
    /**
     * Handle authentication failure
     */
    private void handleAuthenticationFailure() {
        authenticationInProgress = false;
        hideProgress();
        
        failedAttempts++;
        
        // Clear password field
        passwordField.clear();
        
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            showStatus("Too many failed attempts. Access denied.", false);
            
            // Disable login for security
            loginButton.setDisable(true);
            passwordField.setDisable(true);
            
            // Auto-exit after delay
            Platform.runLater(() -> {
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        Platform.runLater(this::exitApplication);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
            
        } else {
            int attemptsLeft = MAX_FAILED_ATTEMPTS - failedAttempts;
            showStatus(String.format("Invalid password. %d attempt(s) remaining.", attemptsLeft), false);
            
            // Focus password field for retry
            passwordField.requestFocus();
        }
    }
    
    /**
     * Show authentication progress
     */
    private void showProgress(String message) {
        authProgress.setVisible(true);
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        loginButton.setDisable(true);
    }
    
    /**
     * Hide authentication progress
     */
    private void hideProgress() {
        authProgress.setVisible(false);
        loginButton.setDisable(false);
    }
    
    /**
     * Show status message
     */
    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        
        // Apply success/error styling
        statusLabel.getStyleClass().removeIf(cls -> cls.equals("success") || cls.equals("error"));
        statusLabel.getStyleClass().add(success ? "success" : "error");
    }
    
    /**
     * Hash password for comparison
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
            
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Password hashing failed", e);
            return password; // Fallback to plain text (not secure)
        }
    }
    
    /**
     * Exit application
     */
    private void exitApplication() {
        if (onAuthenticationCancelled != null) {
            onAuthenticationCancelled.run();
        } else {
            Platform.exit();
        }
    }
    
    /**
     * Show the authentication dialog
     */
    public void show() {
        primaryStage.setScene(scene);
        primaryStage.setTitle("GhostVault - Authentication");
        
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
        
        // Focus password field
        Platform.runLater(() -> passwordField.requestFocus());
    }
    
    /**
     * Reset authentication state
     */
    public void reset() {
        authenticationInProgress = false;
        failedAttempts = 0;
        
        passwordField.clear();
        passwordField.setDisable(false);
        loginButton.setDisable(false);
        
        hideProgress();
        statusLabel.setVisible(false);
    }
    
    // Getters and Setters
    
    public void setOnAuthenticationSuccess(Consumer<ModeController.VaultMode> callback) {
        this.onAuthenticationSuccess = callback;
    }
    
    public void setOnAuthenticationCancelled(Runnable callback) {
        this.onAuthenticationCancelled = callback;
    }
    
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public Scene getScene() {
        return scene;
    }
    
    /**
     * Set custom password hashes (for configuration)
     */
    public void setMasterPasswordHash(String hash) {
        this.masterPasswordHash = hash;
    }
    
    public void setPanicPasswordHash(String hash) {
        this.panicPasswordHash = hash;
    }
    
    public void setDecoyPasswordHash(String hash) {
        this.decoyPasswordHash = hash;
    }
    
    /**
     * Get current failed attempts count
     */
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    /**
     * Check if authentication is in progress
     */
    public boolean isAuthenticationInProgress() {
        return authenticationInProgress;
    }
    
    // Authentication provider for backend integration
    private com.ghostvault.ui.components.AuthenticationProvider authenticationProvider;
    
    /**
     * Set authentication provider for backend integration
     */
    public void setAuthenticationProvider(com.ghostvault.ui.components.AuthenticationProvider provider) {
        this.authenticationProvider = provider;
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (passwordField != null) {
            passwordField.clear();
        }
        // Clear any sensitive data
        if (scene != null) {
            scene = null;
        }
    }

}