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
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            showError("Please enter a password.");
            return;
        }
        
        // Clear password field for security
        passwordField.clear();
        
        // Show loading animation
        if (uiManager != null) {
            uiManager.showLoadingAnimation(loginButton);
        }
        
        // TODO: Integrate with ApplicationIntegrator for authentication
        // For now, show placeholder message
        statusLabel.setText("Authenticating...");
        statusLabel.setStyle("-fx-text-fill: blue;");
        
        // Simulate authentication delay
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    statusLabel.setText("Authentication successful!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
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