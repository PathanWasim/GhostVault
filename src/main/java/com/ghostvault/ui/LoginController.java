package com.ghostvault.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;

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
    @FXML private Button helpButton;
    @FXML private Button exitButton;
    
    private UIManager uiManager;
    private com.ghostvault.integration.ApplicationIntegrator applicationIntegrator;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up theme toggle
        themeToggleButton.setSelected(true); // Default to dark theme
        
        // Focus on password field
        Platform.runLater(() -> passwordField.requestFocus());
        
        // Clear status on password change
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            statusLabel.setText("");
        });
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
            // Run authentication in background thread
            new Thread(() -> {
                try {
                    // Add small delay for better UX
                    Thread.sleep(500);
                    
                    // Authenticate
                    applicationIntegrator.handleAuthentication(passwordCopy);
                    
                    // If we reach here, authentication was successful
                    Platform.runLater(() -> {
                        statusLabel.setText("✓ Access granted!");
                        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("Authentication failed: " + e.getMessage());
                        resetLoginForm();
                    });
                }
            }, "GhostVault-Login").start();
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
}