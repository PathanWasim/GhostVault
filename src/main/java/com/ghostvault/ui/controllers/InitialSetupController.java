package com.ghostvault.ui.controllers;

import com.ghostvault.security.PasswordManager;
import com.ghostvault.ui.components.ModernThemeManager;
import com.ghostvault.ui.components.NotificationSystem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

/**
 * Initial setup wizard for configuring Master, Panic, and Decoy passwords
 */
public class InitialSetupController {
    
    private Stage setupStage;
    private Scene setupScene;
    private VBox rootLayout;
    
    // Password fields - single entry only
    private PasswordField masterPasswordField;
    private PasswordField panicPasswordField;
    private PasswordField decoyPasswordField;
    

    
    // Callbacks
    private Consumer<Boolean> onSetupComplete;
    
    public InitialSetupController(Stage parentStage) {
        initializeSetupStage(parentStage);
        createSetupUI();
    }
    
    private void initializeSetupStage(Stage parentStage) {
        setupStage = new Stage();
        setupStage.initModality(Modality.APPLICATION_MODAL);
        setupStage.initOwner(parentStage);
        setupStage.initStyle(StageStyle.UNDECORATED);
        setupStage.setTitle("GhostVault - Initial Setup");
        setupStage.setResizable(false);
    }
    
    private void createSetupUI() {
        rootLayout = new VBox(12);
        rootLayout.setPadding(new Insets(20));
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.getStyleClass().add("setup-container");
        rootLayout.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 12; -fx-border-color: #475569; -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 12, 0, 0, 4);");
        
        // Header
        VBox header = createHeader();
        
        // Password setup sections
        VBox passwordSections = createPasswordSections();
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        rootLayout.getChildren().addAll(header, passwordSections, actionButtons);
        
        setupScene = new Scene(rootLayout, 500, 650);
        com.ghostvault.ui.theme.PasswordManagerTheme.applyPasswordManagerTheme(setupScene);
        setupStage.setScene(setupScene);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("🔒 GhostVault Setup");
        titleLabel.getStyleClass().add("setup-title");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F8FAFC; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        
        Label subtitleLabel = new Label("Configure your three security passwords - no confirmation required");
        subtitleLabel.getStyleClass().add("setup-subtitle");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #CBD5E1; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        
        Label warningLabel = new Label("⚠️ Remember all passwords - they cannot be recovered");
        warningLabel.getStyleClass().add("setup-warning");
        warningLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F59E0B; -fx-font-weight: 600; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        
        header.getChildren().addAll(titleLabel, subtitleLabel, warningLabel);
        return header;
    }
    

    
    private VBox createPasswordSections() {
        VBox passwordSections = new VBox(15);
        passwordSections.setAlignment(Pos.CENTER);
        
        // Master Password Section
        VBox masterSection = createPasswordSection(
            "Master Password",
            "Full access to your secure vault with all features and real files",
            "#4CAF50"
        );
        masterPasswordField = (PasswordField) masterSection.getChildren().get(2);
        
        // Panic Password Section
        VBox panicSection = createPasswordSection(
            "Panic Password",
            "Emergency mode - securely wipes all data if entered under duress",
            "#f44336"
        );
        panicPasswordField = (PasswordField) panicSection.getChildren().get(2);
        
        // Decoy Password Section
        VBox decoySection = createPasswordSection(
            "Decoy Password",
            "Shows a fake vault with dummy files to protect your real data",
            "#ff9800"
        );
        decoyPasswordField = (PasswordField) decoySection.getChildren().get(2);
        
        passwordSections.getChildren().addAll(masterSection, panicSection, decoySection);
        return passwordSections;
    }
    
    private VBox createPasswordSection(String title, String description, String accentColor) {
        VBox section = new VBox(6);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(12));
        section.setStyle("-fx-background-color: #334155; -fx-background-radius: 12; -fx-border-color: " + accentColor + "; -fx-border-width: 2; -fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: " + accentColor + "; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #CBD5E1; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(380);
        
        // Single password field - no confirmation needed
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter " + title.toLowerCase());
        passwordField.setPrefWidth(380);
        passwordField.setPrefHeight(35);
        passwordField.getStyleClass().add("setup-password-field");
        passwordField.setStyle("-fx-background-color: #475569; -fx-text-fill: #F8FAFC; -fx-prompt-text-fill: #94A3B8; -fx-border-color: " + accentColor + "; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12px 16px; -fx-font-size: 14px; -fx-font-family: 'JetBrains Mono', 'Consolas', monospace;");
        
        // Real-time strength indicator
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(380);
        strengthBar.setPrefHeight(6);
        strengthBar.getStyleClass().add("password-strength");
        strengthBar.setStyle("-fx-accent: " + accentColor + ";");
        
        Label strengthLabel = new Label("Password strength: Weak");
        strengthLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        
        // Password strength validation with visual feedback
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            double strength = calculatePasswordStrength(newText);
            strengthBar.setProgress(strength);
            
            String strengthText;
            String strengthColor;
            if (strength < 0.3) {
                strengthText = "Password strength: Weak";
                strengthColor = "#f44336";
            } else if (strength < 0.7) {
                strengthText = "Password strength: Medium";
                strengthColor = "#ff9800";
            } else {
                strengthText = "Password strength: Strong";
                strengthColor = "#4CAF50";
            }
            
            strengthLabel.setText(strengthText);
            strengthLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + strengthColor + "; -fx-font-weight: 600; -fx-font-family: 'Inter', 'Segoe UI', sans-serif;");
        });
        
        // Add tooltip with password requirements
        Tooltip tooltip = new Tooltip("Recommended: 6+ characters with mix of letters, numbers, and symbols");
        tooltip.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #F8FAFC; -fx-border-color: #475569; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-size: 12px; -fx-font-family: 'Inter', 'Segoe UI', sans-serif; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);");
        passwordField.setTooltip(tooltip);
        
        section.getChildren().addAll(titleLabel, descLabel, passwordField, strengthBar, strengthLabel);
        return section;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));
        
        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("button", "secondary-button");
        cancelButton.setPrefWidth(100);
        cancelButton.setPrefHeight(32);
        cancelButton.setOnAction(e -> handleCancel());
        
        Button setupButton = new Button("Complete Setup");
        setupButton.getStyleClass().addAll("button", "primary-button");
        setupButton.setPrefWidth(140);
        setupButton.setPrefHeight(32);
        setupButton.setOnAction(e -> handleSetupComplete());
        
        buttonBox.getChildren().addAll(cancelButton, setupButton);
        return buttonBox;
    }
    
    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0.0;
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;
        
        // Character variety
        if (password.matches(".*[a-z].*")) score += 10;
        if (password.matches(".*[A-Z].*")) score += 10;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score += 15;
        
        return Math.min(score / 100.0, 1.0);
    }
    

    
    private void handleCancel() {
        setupStage.close();
        if (onSetupComplete != null) {
            onSetupComplete.accept(false);
        }
    }
    
    private void handleSetupComplete() {
        if (!validateAllPasswords()) {
            return;
        }
        
        try {
            // Store the passwords securely
            PasswordManager passwordManager = new PasswordManager();
            
            String masterPassword = masterPasswordField.getText();
            String panicPassword = panicPasswordField.getText();
            String decoyPassword = decoyPasswordField.getText();
            
            // Validate passwords are different
            if (masterPassword.equals(panicPassword) || 
                masterPassword.equals(decoyPassword) || 
                panicPassword.equals(decoyPassword)) {
                NotificationSystem.showError("Setup Error", "All three passwords must be different");
                return;
            }
            
            // Store passwords with proper hashing
            passwordManager.setMasterPassword(masterPassword);
            passwordManager.setPanicPassword(panicPassword);
            passwordManager.setDecoyPassword(decoyPassword);
            
            // Initialize vault directories
            passwordManager.initializeVaultDirectories();
            
            // Mark setup as complete
            passwordManager.markSetupComplete();
            
            NotificationSystem.showSuccess("Setup Complete", "GhostVault has been configured successfully");
            
            setupStage.close();
            if (onSetupComplete != null) {
                onSetupComplete.accept(true);
            }
            
        } catch (Exception e) {
            NotificationSystem.showError("Setup Failed", "Failed to complete setup: " + e.getMessage());
        }
    }
    
    private boolean validateAllPasswords() {
        // Validate Master Password
        if (!validatePassword(masterPasswordField, "Master")) {
            return false;
        }
        
        // Validate Panic Password
        if (!validatePassword(panicPasswordField, "Panic")) {
            return false;
        }
        
        // Validate Decoy Password
        if (!validatePassword(decoyPasswordField, "Decoy")) {
            return false;
        }
        
        return true;
    }
    
    private boolean validatePassword(PasswordField passwordField, String type) {
        String password = passwordField.getText();
        
        if (password == null || password.trim().isEmpty()) {
            NotificationSystem.showError("Validation Error", type + " password cannot be empty");
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            NotificationSystem.showError("Validation Error", type + " password must be at least 6 characters long");
            passwordField.requestFocus();
            return false;
        }
        
        // Check password strength
        double strength = calculatePasswordStrength(password);
        if (strength < 0.3) {
            NotificationSystem.showWarning("Weak Password", type + " password is weak. Consider using a stronger password with mixed characters.");
            // Don't block setup, just warn
        }
        
        return true;
    }
    
    public void show() {
        setupStage.showAndWait();
    }
    
    public void setOnSetupComplete(Consumer<Boolean> callback) {
        this.onSetupComplete = callback;
    }
}