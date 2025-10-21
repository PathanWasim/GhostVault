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
    
    // Password fields
    private PasswordField masterPasswordField;
    private PasswordField masterConfirmField;
    private PasswordField panicPasswordField;
    private PasswordField panicConfirmField;
    private PasswordField decoyPasswordField;
    private PasswordField decoyConfirmField;
    

    
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
        rootLayout.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 10;");
        
        // Header
        VBox header = createHeader();
        
        // Password setup sections
        VBox passwordSections = createPasswordSections();
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        rootLayout.getChildren().addAll(header, passwordSections, actionButtons);
        
        setupScene = new Scene(rootLayout, 480, 580);
        ModernThemeManager.applyTheme(setupScene);
        setupStage.setScene(setupScene);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("🔒 GhostVault Setup");
        titleLabel.getStyleClass().add("setup-title");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitleLabel = new Label("Configure your three security passwords");
        subtitleLabel.getStyleClass().add("setup-subtitle");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #cccccc;");
        
        Label warningLabel = new Label("⚠️ Remember all passwords - they cannot be recovered");
        warningLabel.getStyleClass().add("setup-warning");
        warningLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ff9800; -fx-font-weight: bold;");
        
        header.getChildren().addAll(titleLabel, subtitleLabel, warningLabel);
        return header;
    }
    

    
    private VBox createPasswordSections() {
        VBox passwordSections = new VBox(10);
        passwordSections.setAlignment(Pos.CENTER);
        
        // Master Password Section
        VBox masterSection = createPasswordSection(
            "Master Password",
            "Access to your real secure vault",
            "#4CAF50"
        );
        HBox masterPasswordRow = (HBox) masterSection.getChildren().get(2);
        masterPasswordField = (PasswordField) masterPasswordRow.getChildren().get(0);
        masterConfirmField = (PasswordField) masterPasswordRow.getChildren().get(1);
        
        // Panic Password Section
        VBox panicSection = createPasswordSection(
            "Panic Password",
            "Emergency wipe - destroys all data permanently",
            "#f44336"
        );
        HBox panicPasswordRow = (HBox) panicSection.getChildren().get(2);
        panicPasswordField = (PasswordField) panicPasswordRow.getChildren().get(0);
        panicConfirmField = (PasswordField) panicPasswordRow.getChildren().get(1);
        
        // Decoy Password Section
        VBox decoySection = createPasswordSection(
            "Decoy Password",
            "Shows fake vault to protect under duress",
            "#ff9800"
        );
        HBox decoyPasswordRow = (HBox) decoySection.getChildren().get(2);
        decoyPasswordField = (PasswordField) decoyPasswordRow.getChildren().get(0);
        decoyConfirmField = (PasswordField) decoyPasswordRow.getChildren().get(1);
        
        passwordSections.getChildren().addAll(masterSection, panicSection, decoySection);
        return passwordSections;
    }
    
    private VBox createPasswordSection(String title, String description, String accentColor) {
        VBox section = new VBox(4);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(8));
        section.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 6; -fx-border-color: " + accentColor + "; -fx-border-width: 1; -fx-border-radius: 6;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #cccccc;");
        descLabel.setWrapText(true);
        
        HBox passwordRow = new HBox(8);
        passwordRow.setAlignment(Pos.CENTER);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefWidth(180);
        passwordField.setPrefHeight(28);
        passwordField.getStyleClass().add("setup-password-field");
        
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");
        confirmField.setPrefWidth(180);
        confirmField.setPrefHeight(28);
        confirmField.getStyleClass().add("setup-password-field");
        
        passwordRow.getChildren().addAll(passwordField, confirmField);
        
        // Simplified strength indicator
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(370);
        strengthBar.setPrefHeight(4);
        strengthBar.getStyleClass().add("password-strength");
        
        // Password strength validation
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            double strength = calculatePasswordStrength(newText);
            strengthBar.setProgress(strength);
        });
        
        section.getChildren().addAll(titleLabel, descLabel, passwordRow, strengthBar);
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
        if (!validatePasswordPair(masterPasswordField, masterConfirmField, "Master")) {
            return false;
        }
        
        // Validate Panic Password
        if (!validatePasswordPair(panicPasswordField, panicConfirmField, "Panic")) {
            return false;
        }
        
        // Validate Decoy Password
        if (!validatePasswordPair(decoyPasswordField, decoyConfirmField, "Decoy")) {
            return false;
        }
        
        return true;
    }
    
    private boolean validatePasswordPair(PasswordField passwordField, PasswordField confirmField, String type) {
        String password = passwordField.getText();
        String confirm = confirmField.getText();
        
        if (password == null || password.trim().isEmpty()) {
            NotificationSystem.showError("Validation Error", type + " password cannot be empty");
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < 8) {
            NotificationSystem.showError("Validation Error", type + " password must be at least 8 characters long");
            passwordField.requestFocus();
            return false;
        }
        
        if (!password.equals(confirm)) {
            NotificationSystem.showError("Validation Error", type + " passwords do not match");
            confirmField.requestFocus();
            return false;
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