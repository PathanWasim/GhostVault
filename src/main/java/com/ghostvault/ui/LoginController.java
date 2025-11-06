package com.ghostvault.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login screen
 * Handles user authentication and theme switching
 */
public class LoginController implements Initializable {
    
    @FXML private ToggleButton themeToggleButton;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;
    @FXML private Label lockoutLabel;
    @FXML private ProgressBar lockoutProgressBar;
    @FXML private Button helpButton;
    @FXML private Button exitButton;
    
    private UIManager uiManager;
    private com.ghostvault.integration.ApplicationIntegrator applicationIntegrator;
    
    // Lockout countdown timer
    private javafx.animation.Timeline lockoutTimer;
    private boolean isLockoutActive = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up theme toggle
        themeToggleButton.setSelected(true); // Default to dark theme
        
        // Focus on password field
        Platform.runLater(() -> passwordField.requestFocus());
        
        // Clear status on password change
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isLockoutActive) {
                statusLabel.setText("");
            }
        });
        
        // Initialize lockout UI components (may be null if not in FXML)
        if (lockoutLabel != null) {
            lockoutLabel.setVisible(false);
        }
        if (lockoutProgressBar != null) {
            lockoutProgressBar.setVisible(false);
        }
    }
    
    /**
     * Set the UI manager reference
     */
    public void setUIManager(UIManager uiManager) {
        this.uiManager = uiManager;
        
        // Sync theme toggle with UI manager
        if (uiManager != null) {
            themeToggleButton.setSelected(uiManager.isDarkTheme());
        }
    }
    
    /**
     * Set the application integrator reference
     */
    public void setApplicationIntegrator(com.ghostvault.integration.ApplicationIntegrator integrator) {
        this.applicationIntegrator = integrator;
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        // Check for lockout status first
        if (applicationIntegrator != null && applicationIntegrator.getSecurityAttemptManager().isLocked()) {
            startLockoutCountdown();
            return;
        }
        
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            showError("Please enter a password.");
            return;
        }
        
        // Disable login button during authentication
        loginButton.setDisable(true);
        passwordField.setDisable(true);
        
        // Show professional authenticating status
        statusLabel.setText("🔐 Authenticating...");
        statusLabel.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        
        // Clear password field for security immediately
        String passwordCopy = password;
        passwordField.clear();
        
        // Authenticate asynchronously to avoid blocking UI
        if (applicationIntegrator != null) {
            // Delegate to ApplicationIntegrator - it will handle all outcomes
            applicationIntegrator.handleAuthentication(passwordCopy);
            
            // Note: The ApplicationIntegrator will:
            // - Show success and navigate to vault on valid password
            // - Show error notification on invalid password
            // - Handle panic/decoy passwords appropriately
            
            // Reset form after a delay to allow retry
            // (ApplicationIntegrator will handle navigation on success)
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Wait for authentication to complete
                    Platform.runLater(() -> {
                        // Only reset if we're still on login screen
                        // (if successful, we'll have navigated away)
                        if (loginButton.isDisabled()) {
                            resetLoginForm();
                            
                            // Check if we need to start lockout countdown
                            if (applicationIntegrator.getSecurityAttemptManager().isLocked()) {
                                startLockoutCountdown();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "GhostVault-LoginReset").start();
        } else {
            showError("Application not properly initialized.");
            resetLoginForm();
        }
    }
    
    /**
     * Reset login form to initial state
     */
    private void resetLoginForm() {
        loginButton.setDisable(false);
        passwordField.setDisable(false);
        passwordField.requestFocus();
    }
    
    /**
     * Toggle between dark and light themes
     */
    @FXML
    private void toggleTheme() {
        if (uiManager != null) {
            uiManager.toggleTheme();
        }
        
        // Update button text
        themeToggleButton.setText(themeToggleButton.isSelected() ? "🌙" : "☀️");
    }
    
    /**
     * Show help dialog
     */
    @FXML
    private void showHelp() {
        if (uiManager != null) {
            // Show login help
            Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
            helpAlert.setTitle("Login Help");
            helpAlert.setHeaderText("GhostVault Login");
            helpAlert.setContentText(
                "Enter one of your three passwords:\n\n" +
                "• Master Password: Access your secure vault\n" +
                "• Panic Password: Emergency data destruction\n" +
                "• Decoy Password: Show fake files\n\n" +
                "Press F1 for complete help documentation.\n" +
                "Use the moon/sun button to toggle themes."
            );
            helpAlert.showAndWait();
        }
    }
    
    /**
     * Handle exit button
     */
    @FXML
    private void handleExit() {
        Platform.exit();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
        
        if (uiManager != null) {
            uiManager.showErrorAnimation(passwordField);
        }
    }
    
    /**
     * Start lockout countdown timer
     */
    private void startLockoutCountdown() {
        if (applicationIntegrator == null) return;
        
        var securityManager = applicationIntegrator.getSecurityAttemptManager();
        startLockoutCountdown(securityManager);
    }
    
    /**
     * Start lockout countdown timer with provided SecurityAttemptManager
     */
    public void startLockoutCountdown(com.ghostvault.security.SecurityAttemptManager securityManager) {
        if (securityManager == null || !securityManager.isLocked()) return;
        
        isLockoutActive = true;
        
        // Disable login controls
        loginButton.setDisable(true);
        passwordField.setDisable(true);
        
        // Show lockout UI elements
        if (lockoutLabel != null) {
            lockoutLabel.setVisible(true);
        }
        if (lockoutProgressBar != null) {
            lockoutProgressBar.setVisible(true);
        }
        
        // Stop any existing timer
        if (lockoutTimer != null) {
            lockoutTimer.stop();
        }
        
        // Create countdown timer
        lockoutTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateLockoutDisplay()));
        lockoutTimer.setCycleCount(Timeline.INDEFINITE);
        lockoutTimer.play();
        
        // Initial display update
        updateLockoutDisplay();
    }
    
    /**
     * Update lockout display with remaining time
     */
    private void updateLockoutDisplay() {
        if (applicationIntegrator == null) return;
        
        var securityManager = applicationIntegrator.getSecurityAttemptManager();
        
        if (!securityManager.isLocked()) {
            // Lockout expired
            endLockoutCountdown();
            return;
        }
        
        int remainingSeconds = securityManager.getRemainingLockoutSeconds();
        long totalDuration = securityManager.getLockoutDuration() / 1000;
        
        // Update status label
        statusLabel.setText(String.format("🔒 Account locked - %d seconds remaining", remainingSeconds));
        statusLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
        
        // Update lockout label if available
        if (lockoutLabel != null) {
            lockoutLabel.setText(String.format("Security lockout active: %d seconds", remainingSeconds));
            lockoutLabel.setStyle("-fx-text-fill: #ff6b6b;");
        }
        
        // Update progress bar if available
        if (lockoutProgressBar != null) {
            double progress = (double) remainingSeconds / totalDuration;
            lockoutProgressBar.setProgress(progress);
            lockoutProgressBar.setStyle("-fx-accent: #ff6b6b;");
        }
    }
    
    /**
     * End lockout countdown and re-enable login
     */
    private void endLockoutCountdown() {
        isLockoutActive = false;
        
        // Stop timer
        if (lockoutTimer != null) {
            lockoutTimer.stop();
            lockoutTimer = null;
        }
        
        // Hide lockout UI elements
        if (lockoutLabel != null) {
            lockoutLabel.setVisible(false);
        }
        if (lockoutProgressBar != null) {
            lockoutProgressBar.setVisible(false);
        }
        
        // Re-enable login controls
        loginButton.setDisable(false);
        passwordField.setDisable(false);
        
        // Clear status and focus password field
        statusLabel.setText("");
        passwordField.requestFocus();
    }
    
    /**
     * Show lockout warning message
     */
    public void showLockoutWarning(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ff9800; -fx-font-weight: bold;");
        
        if (uiManager != null) {
            uiManager.showErrorAnimation(passwordField);
        }
    }
    
    /**
     * Update attempt counter display
     */
    public void updateAttemptDisplay(int attempts, int maxAttempts) {
        if (attempts > 0) {
            int remaining = maxAttempts - attempts;
            String message = String.format("⚠️ %d/%d attempts used - %d remaining", 
                attempts, maxAttempts, remaining);
            
            if (remaining <= 1) {
                statusLabel.setText(message);
                statusLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
            } else {
                statusLabel.setText(message);
                statusLabel.setStyle("-fx-text-fill: #ff9800;");
            }
        }
    }
}