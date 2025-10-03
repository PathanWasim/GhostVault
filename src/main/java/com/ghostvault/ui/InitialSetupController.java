package com.ghostvault.ui;

import com.ghostvault.security.PasswordManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.application.Platform;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the initial setup screen
 * Handles password creation and vault initialization
 */
public class InitialSetupController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private Label instructionLabel;
    
    @FXML private PasswordField masterPasswordField;
    @FXML private ProgressBar masterStrengthBar;
    @FXML private Label masterStrengthLabel;
    
    @FXML private PasswordField panicPasswordField;
    @FXML private ProgressBar panicStrengthBar;
    @FXML private Label panicStrengthLabel;
    
    @FXML private PasswordField decoyPasswordField;
    @FXML private ProgressBar decoyStrengthBar;
    @FXML private Label decoyStrengthLabel;
    
    @FXML private Button createVaultButton;
    @FXML private Label statusLabel;
    @FXML private Button helpButton;
    @FXML private Button exitButton;
    
    private UIManager uiManager;
    private PasswordStrengthMeter strengthMeter;
    private PasswordManager passwordManager;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        strengthMeter = new PasswordStrengthMeter();
        
        // Set up password strength listeners
        masterPasswordField.textProperty().addListener((obs, oldVal, newVal) -> 
            updatePasswordStrength(newVal, masterStrengthBar, masterStrengthLabel));
        panicPasswordField.textProperty().addListener((obs, oldVal, newVal) -> 
            updatePasswordStrength(newVal, panicStrengthBar, panicStrengthLabel));
        decoyPasswordField.textProperty().addListener((obs, oldVal, newVal) -> 
            updatePasswordStrength(newVal, decoyStrengthBar, decoyStrengthLabel));
        
        // Focus on first password field
        Platform.runLater(() -> masterPasswordField.requestFocus());
    }
    
    /**
     * Set the UI manager reference
     */
    public void setUIManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    /**
     * Set the password manager reference
     */
    public void setPasswordManager(PasswordManager passwordManager) {
        this.passwordManager = passwordManager;
    }
    
    /**
     * Handle create vault button click
     */
    @FXML
    private void handleCreateVault() {
        String masterPassword = masterPasswordField.getText();
        String panicPassword = panicPasswordField.getText();
        String decoyPassword = decoyPasswordField.getText();
        
        if (validatePasswords(masterPassword, panicPassword, decoyPassword)) {
            try {
                // Clear status
                statusLabel.setText("Creating vault...");
                statusLabel.setStyle("-fx-text-fill: #2196F3;");
                
                // Disable form during creation
                createVaultButton.setDisable(true);
                masterPasswordField.setDisable(true);
                panicPasswordField.setDisable(true);
                decoyPasswordField.setDisable(true);
                
                // Create passwords in password manager
                if (passwordManager != null) {
                    passwordManager.initializePasswords(
                        masterPassword.toCharArray(),
                        panicPassword.toCharArray(),
                        decoyPassword.toCharArray()
                    );
                }
                
                // Show success message
                statusLabel.setText("✓ Vault created successfully! Redirecting to login...");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                
                // Navigate to login after short delay
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(1500);
                        if (uiManager != null) {
                            uiManager.showLoginScene();
                        }
                    } catch (Exception e) {
                        statusLabel.setText("Error: " + e.getMessage());
                        statusLabel.setStyle("-fx-text-fill: #f44336;");
                        createVaultButton.setDisable(false);
                        masterPasswordField.setDisable(false);
                        panicPasswordField.setDisable(false);
                        decoyPasswordField.setDisable(false);
                    }
                });
                
            } catch (Exception e) {
                statusLabel.setText("Error creating vault: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: #f44336;");
                createVaultButton.setDisable(false);
                masterPasswordField.setDisable(false);
                panicPasswordField.setDisable(false);
                decoyPasswordField.setDisable(false);
            }
        }
    }
    
    /**
     * Validate all passwords
     */
    private boolean validatePasswords(String master, String panic, String decoy) {
        // Clear previous status
        statusLabel.setStyle("-fx-text-fill: red;");
        
        if (master.isEmpty() || panic.isEmpty() || decoy.isEmpty()) {
            statusLabel.setText("All password fields are required.");
            return false;
        }
        
        if (PasswordManager.getPasswordStrength(master) < 4) {
            statusLabel.setText("Master password is too weak. Minimum requirements not met.");
            return false;
        }
        
        if (PasswordManager.getPasswordStrength(panic) < 3) {
            statusLabel.setText("Panic password is too weak. Minimum 3/5 strength required.");
            return false;
        }
        
        if (PasswordManager.getPasswordStrength(decoy) < 3) {
            statusLabel.setText("Decoy password is too weak. Minimum 3/5 strength required.");
            return false;
        }
        
        if (master.equals(panic) || master.equals(decoy) || panic.equals(decoy)) {
            statusLabel.setText("All passwords must be different from each other.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Update password strength indicator
     */
    private void updatePasswordStrength(String password, ProgressBar strengthBar, Label strengthLabel) {
        int score = PasswordManager.getPasswordStrength(password);
        double progress = score / 5.0;
        strengthBar.setProgress(progress);
        
        String strengthText = PasswordManager.getPasswordStrengthDescription(score);
        String color = PasswordManager.getPasswordStrengthColor(score);
        
        String feedback = PasswordManager.getPasswordStrengthFeedback(password);
        strengthLabel.setText(strengthText + (feedback.equals("Strong password!") ? "" : " - " + feedback));
        strengthLabel.setStyle("-fx-text-fill: " + color + ";");
        
        // Update progress bar color
        strengthBar.setStyle("-fx-accent: " + color + ";");
    }
    
    /**
     * Get color for strength level
     */
    private String getStrengthColor(int score) { return PasswordManager.getPasswordStrengthColor(score); }
    
    /**
     * Show help dialog
     */
    @FXML
    private void showHelp() {
        if (uiManager != null) {
            // Show help for initial setup
            Alert helpAlert = new Alert(Alert.AlertType.INFORMATION);
            helpAlert.setTitle("Initial Setup Help");
            helpAlert.setHeaderText("Setting up your GhostVault");
            helpAlert.setContentText(
                "Create three different passwords:\n\n" +
                "• Master Password: Full access to your vault\n" +
                "• Panic Password: Emergency data destruction\n" +
                "• Decoy Password: Shows fake files\n\n" +
                "All passwords must be strong and different from each other.\n" +
                "Remember your master password - it cannot be recovered!"
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
}