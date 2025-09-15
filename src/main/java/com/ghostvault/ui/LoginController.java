package com.ghostvault.ui;

import com.ghostvault.integration.ApplicationIntegrator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

/**
 * Controller for the login interface
 * Handles user authentication and password entry
 */
public class LoginController {
    
    @FXML private Label titleLabel;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label statusLabel;
    @FXML private ToggleButton themeToggle;
    
    private UIManager uiManager;
    private ApplicationIntegrator applicationIntegrator;
    
    /**
     * Initialize the login controller
     */
    @FXML
    private void initialize() {
        // Set up password field
        if (passwordField != null) {
            passwordField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleLogin();
                }
            });
        }
        
        // Set up theme toggle
        if (themeToggle != null) {
            themeToggle.setSelected(true); // Default to dark theme
            themeToggle.setText(themeToggle.isSelected() ? "🌙" : "☀️");
        }
        
        // Clear status initially
        if (statusLabel != null) {
            statusLabel.setText("");
        }
    }
    
    /**
     * Set UI manager reference
     */
    public void setUIManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    /**
     * Set application integrator reference
     */
    public void setApplicationIntegrator(ApplicationIntegrator applicationIntegrator) {
        this.applicationIntegrator = applicationIntegrator;
    }
    
    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        if (passwordField == null) return;
        
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            showStatus("Please enter a password", true);
            return;
        }
        
        // Clear status and disable login button during authentication
        showStatus("Authenticating...", false);
        loginButton.setDisabled(true);
        
        // Pass authentication to application integrator
        if (applicationIntegrator != null) {
            applicationIntegrator.handleAuthentication(password);
        }
        
        // Clear password field for security
        passwordField.clear();
        
        // Re-enable login button after a short delay
        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            loginButton.setDisabled(false);
        });
    }
    
    /**
     * Handle theme toggle
     */
    @FXML
    private void handleThemeToggle() {
        if (uiManager != null && themeToggle != null) {
            uiManager.setDarkTheme(themeToggle.isSelected());
            themeToggle.setText(themeToggle.isSelected() ? "🌙" : "☀️");
        }
    }
    
    /**
     * Show status message
     */
    public void showStatus(String message, boolean isError) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            if (isError) {
                statusLabel.setStyle("-fx-text-fill: #ff4444;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #888888;");
            }
        }
    }
    
    /**
     * Clear status message
     */
    public void clearStatus() {
        if (statusLabel != null) {
            statusLabel.setText("");
        }
    }
    
    /**
     * Focus on password field
     */
    public void focusPasswordField() {
        if (passwordField != null) {
            javafx.application.Platform.runLater(() -> passwordField.requestFocus());
        }
    }
    
    /**
     * Show login error
     */
    public void showLoginError(String message) {
        showStatus(message, true);
        
        // Clear error after a few seconds
        javafx.concurrent.Task<Void> clearTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(5000); // Wait 5 seconds
                return null;
            }
            
            @Override
            protected void succeeded() {
                clearStatus();
            }
        };
        
        Thread clearThread = new Thread(clearTask);
        clearThread.setDaemon(true);
        clearThread.start();
    }
}