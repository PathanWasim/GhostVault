package com.ghostvault.ui;

import com.ghostvault.security.PasswordManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the initial setup interface
 * Handles first-run password configuration
 */
public class InitialSetupController {
    
    @FXML private Label titleLabel;
    @FXML private Label instructionLabel;
    
    // Master password fields
    @FXML private PasswordField masterPasswordField;
    @FXML private ProgressBar masterStrengthBar;
    @FXML private Label masterStrengthLabel;
    
    // Panic password fields
    @FXML private PasswordField panicPasswordField;
    @FXML private ProgressBar panicStrengthBar;
    @FXML private Label panicStrengthLabel;
    
    // Decoy password fields
    @FXML private PasswordField decoyPasswordField;
    @FXML private ProgressBar decoyStrengthBar;
    @FXML private Label decoyStrengthLabel;
    
    @FXML private Button createVaultButton;
    @FXML private Label statusLabel;
    
    private UIManager uiManager;
    private PasswordManager passwordManager;
    
    /**
     * Initialize the setup controller
     */
    @FXML
    private void initialize() {
        // Set up password strength listeners
        if (masterPasswordField != null) {
            masterPasswordField.textProperty().addListener((obs, oldVal, newVal) -> 
                updatePasswordStrength(newVal, masterStrengthBar, masterStrengthLabel));
        }
        
        if (panicPasswordField != null) {
            panicPasswordField.textProperty().addListener((obs, oldVal, newVal) -> 
                updatePasswordStrength(newVal, panicStrengthBar, panicStrengthLabel));
        }
        
        if (decoyPasswordField != null) {
            decoyPasswordField.textProperty().addListener((obs, oldVal, newVal) -> 
                updatePasswordStrength(newVal, decoyStrengthBar, decoyStrengthLabel));
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
     * Set password manager reference
     */
    public void setPasswordManager(PasswordManager passwordManager) {
        this.passwordManager = passwordManager;
    }
    
    /**
     * Handle create vault button click
     */
    @FXML
    private void handleCreateVault() {
        if (!validateInputs()) {
            return;
        }
        
        String masterPassword = masterPasswordField.getText();
        String panicPassword = panicPasswordField.getText();
        String decoyPassword = decoyPasswordField.getText();
        
        try {
            // Initialize passwords
            if (passwordManager != null) {
                passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
            }
            
            // Clear password fields for security
            clearPasswordFields();
            
            // Show success and transition to login
            if (uiManager != null) {
                uiManager.showInfo("Setup Complete", "Vault created successfully! You can now log in.");
                uiManager.switchToScene(uiManager.createLoginScene());
            }
            
        } catch (Exception e) {
            showStatus("Failed to create vault: " + e.getMessage(), true);
        }
    }
    
    /**
     * Validate all inputs
     */
    private boolean validateInputs() {
        String masterPassword = masterPasswordField.getText();
        String panicPassword = panicPasswordField.getText();
        String decoyPassword = decoyPasswordField.getText();
        
        // Check if all fields are filled
        if (masterPassword.isEmpty() || panicPassword.isEmpty() || decoyPassword.isEmpty()) {
            showStatus("All password fields are required.", true);
            return false;
        }
        
        // Check password strength
        if (getPasswordStrengthScore(masterPassword) < 4) {
            showStatus("Master password is too weak. Minimum requirements not met.", true);
            return false;
        }
        
        if (getPasswordStrengthScore(panicPassword) < 3) {
            showStatus("Panic password is too weak. Minimum 3/5 strength required.", true);
            return false;
        }
        
        if (getPasswordStrengthScore(decoyPassword) < 3) {
            showStatus("Decoy password is too weak. Minimum 3/5 strength required.", true);
            return false;
        }
        
        // Check that all passwords are different
        if (masterPassword.equals(panicPassword) || masterPassword.equals(decoyPassword) || 
            panicPassword.equals(decoyPassword)) {
            showStatus("All passwords must be different from each other.", true);
            return false;
        }
        
        return true;
    }
    
    /**
     * Update password strength indicator
     */
    private void updatePasswordStrength(String password, ProgressBar strengthBar, Label strengthLabel) {
        if (strengthBar == null || strengthLabel == null) return;
        
        int score = getPasswordStrengthScore(password);
        double progress = score / 5.0;
        
        strengthBar.setProgress(progress);
        
        String[] strengthTexts = {"Very Weak", "Weak", "Fair", "Good", "Strong", "Very Strong"};
        String[] strengthColors = {"#ff4444", "#ff8800", "#ffaa00", "#88aa00", "#44aa44", "#00aa44"};
        
        if (password.isEmpty()) {
            strengthLabel.setText("");
            strengthBar.setStyle("");
        } else {
            strengthLabel.setText(strengthTexts[score]);
            strengthLabel.setStyle("-fx-text-fill: " + strengthColors[score] + ";");
            strengthBar.setStyle("-fx-accent: " + strengthColors[score] + ";");
        }
    }
    
    /**
     * Calculate password strength score (0-5)
     */
    private int getPasswordStrengthScore(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety checks
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*[0-9].*")) score++; // numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++; // special chars
        
        // Bonus for very long passwords
        if (password.length() >= 16) score++;
        
        return Math.min(score, 5);
    }
    
    /**
     * Show status message
     */
    private void showStatus(String message, boolean isError) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            if (isError) {
                statusLabel.setStyle("-fx-text-fill: #ff4444;");
            } else {
                statusLabel.setStyle("-fx-text-fill: #44aa44;");
            }
        }
    }
    
    /**
     * Clear all password fields for security
     */
    private void clearPasswordFields() {
        if (masterPasswordField != null) masterPasswordField.clear();
        if (panicPasswordField != null) panicPasswordField.clear();
        if (decoyPasswordField != null) decoyPasswordField.clear();
    }
}