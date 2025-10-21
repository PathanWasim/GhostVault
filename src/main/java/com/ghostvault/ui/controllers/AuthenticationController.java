package com.ghostvault.ui.controllers;

import com.ghostvault.security.PasswordManager;
import com.ghostvault.ui.components.ErrorHandlingSystem;
import com.ghostvault.ui.components.ModernThemeManager;
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
    
    // Password management
    private PasswordManager passwordManager;
    
    // Callbacks
    private Consumer<ModeController.VaultMode> onAuthenticationSuccess;
    private Runnable onAuthenticationCancelled;
    
    // State
    private boolean authenticationInProgress = false;
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    
    public AuthenticationController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.passwordManager = new PasswordManager();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    /**
     * Check if initial setup is required
     */
    public boolean isSetupRequired() {
        return !passwordManager.isSetupComplete();
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
        ModernThemeManager.applyTheme(scene);
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
                Thread.sleep(800);
                
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
     * Detect vault mode based on password using PasswordManager
     */
    private ModeController.VaultMode detectMode(String password) {
        try {
            return passwordManager.authenticatePassword(password);
        } catch (Exception e) {
            ErrorHandlingSystem.handleError("Authentication error", e);
            return null;
        }
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
        if (authProgress != null) {
            authProgress.setVisible(true);
        }
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
            statusLabel.setVisible(true);
        }
        // Note: loginButton is local in showLoginScreen, so we can't disable it here
    }
    
    /**
     * Hide authentication progress
     */
    private void hideProgress() {
        if (authProgress != null) {
            authProgress.setVisible(false);
        }
        // Note: loginButton is local in showLoginScreen, so we can't enable it here
    }
    
    /**
     * Show status message
     */
    private void showStatus(String message, boolean success) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setVisible(true);
            
            // Apply success/error styling
            if (success) {
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
            } else {
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff6b6b;");
            }
        }
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
     * Show the authentication dialog or setup wizard
     */
    public void show() {
        // Check if initial setup is required
        if (isSetupRequired()) {
            showInitialSetup();
        } else {
            showLoginScreen();
        }
    }
    
    /**
     * Show initial setup wizard
     */
    private void showInitialSetup() {
        System.out.println("🔧 Showing initial setup wizard...");
        InitialSetupController setupController = new InitialSetupController(primaryStage);
        setupController.setOnSetupComplete(success -> {
            if (success) {
                // Setup completed, show login screen
                System.out.println("✅ Setup completed, showing login screen...");
                showLoginScreen();
            } else {
                // Setup cancelled, exit application
                System.out.println("❌ Setup cancelled, exiting...");
                Platform.exit();
            }
        });
        setupController.show();
    }
    
    /**
     * Show login screen
     */
    private void showLoginScreen() {
        System.out.println("🔐 Showing login screen...");
        
        try {
            // Create properly aligned login screen
            VBox loginRoot = new VBox(20);
            loginRoot.setAlignment(Pos.CENTER);
            loginRoot.setPadding(new Insets(40));
            loginRoot.setStyle("-fx-background-color: #1e1e1e;");
            
            // Header section
            VBox headerSection = new VBox(8);
            headerSection.setAlignment(Pos.CENTER);
            
            Label titleLabel = new Label("🔒 GhostVault");
            titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            Label subtitleLabel = new Label("Enter your vault password");
            subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");
            
            headerSection.getChildren().addAll(titleLabel, subtitleLabel);
            
            // Login form section
            VBox formSection = new VBox(15);
            formSection.setAlignment(Pos.CENTER);
            formSection.setMaxWidth(400);
            formSection.setPadding(new Insets(20));
            formSection.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 10; -fx-border-color: #444444; -fx-border-width: 1; -fx-border-radius: 10;");
            
            // Password field
            passwordField = new PasswordField();
            passwordField.setPromptText("Enter your vault password");
            passwordField.setPrefWidth(350);
            passwordField.setPrefHeight(45);
            passwordField.setStyle("-fx-font-size: 14px; -fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-background-radius: 6; -fx-border-color: #555555; -fx-border-width: 1; -fx-border-radius: 6;");
            
            // Button section
            HBox buttonSection = new HBox(12);
            buttonSection.setAlignment(Pos.CENTER);
            
            Button exitButton = new Button("Exit");
            exitButton.setPrefWidth(100);
            exitButton.setPrefHeight(40);
            exitButton.setStyle("-fx-font-size: 14px; -fx-background-color: #666666; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6;");
            exitButton.setOnMouseEntered(e -> exitButton.setStyle("-fx-font-size: 14px; -fx-background-color: #777777; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6;"));
            exitButton.setOnMouseExited(e -> exitButton.setStyle("-fx-font-size: 14px; -fx-background-color: #666666; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6;"));
            
            Button loginButton = new Button("Unlock Vault");
            loginButton.setPrefWidth(150);
            loginButton.setPrefHeight(40);
            loginButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-weight: bold;");
            loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-font-size: 14px; -fx-background-color: #5CBF60; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-weight: bold;"));
            loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-weight: bold;"));
            
            buttonSection.getChildren().addAll(exitButton, loginButton);
            
            // Status section
            VBox statusSection = new VBox(8);
            statusSection.setAlignment(Pos.CENTER);
            
            authProgress = new ProgressIndicator();
            authProgress.setPrefSize(24, 24);
            authProgress.setVisible(false);
            
            statusLabel = new Label("");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff6b6b;");
            statusLabel.setVisible(false);
            
            statusSection.getChildren().addAll(authProgress, statusLabel);
            
            formSection.getChildren().addAll(passwordField, buttonSection, statusSection);
            
            // Footer section
            Label footerLabel = new Label("🛡️ Your data is protected with military-grade encryption");
            footerLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
            
            loginRoot.getChildren().addAll(headerSection, formSection, footerLabel);
            
            // Set up event handlers
            loginButton.setOnAction(e -> {
                String password = passwordField.getText();
                System.out.println("🔐 Login attempted with password: " + (password.isEmpty() ? "[empty]" : "[" + password.length() + " chars]"));
                authenticate();
            });
            
            exitButton.setOnAction(e -> {
                System.out.println("🚪 Exit button clicked");
                Platform.exit();
            });
            
            passwordField.setOnAction(e -> {
                String password = passwordField.getText();
                System.out.println("🔐 Enter key pressed with password: " + (password.isEmpty() ? "[empty]" : "[" + password.length() + " chars]"));
                authenticate();
            });
            
            // Create scene
            Scene loginScene = new Scene(loginRoot, 600, 500);
            ModernThemeManager.applyTheme(loginScene);
            
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("GhostVault - Secure File Management");
            primaryStage.setResizable(false);
            
            if (!primaryStage.isShowing()) {
                primaryStage.show();
                System.out.println("✅ Login screen displayed");
            }
            
            // Focus password field
            Platform.runLater(() -> passwordField.requestFocus());
            
        } catch (Exception e) {
            System.err.println("❌ Error showing login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reset authentication state
     */
    public void reset() {
        authenticationInProgress = false;
        failedAttempts = 0;
        
        if (passwordField != null) {
            passwordField.clear();
            passwordField.setDisable(false);
        }
        
        if (loginButton != null) {
            loginButton.setDisable(false);
        }
        
        hideProgress();
        
        if (statusLabel != null) {
            statusLabel.setVisible(false);
        }
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
        if (passwordManager != null) {
            passwordManager.cleanup();
        }
        // Clear any sensitive data
        if (scene != null) {
            scene = null;
        }
    }

}