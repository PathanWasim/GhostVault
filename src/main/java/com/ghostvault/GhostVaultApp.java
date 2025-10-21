package com.ghostvault;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Clean, simple GhostVault application with proper three-password system
 */
public class GhostVaultApp extends Application {
    
    private Stage primaryStage;
    private boolean isSetupComplete = false;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        System.out.println("🚀 Starting GhostVault...");
        
        // Setup window
        primaryStage.setTitle("GhostVault - Secure File Management");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
        
        // Check if setup is needed
        if (!isSetupComplete) {
            showSetupWizard();
        } else {
            showLoginScreen();
        }
    }
    
    /**
     * Show the three-password setup wizard
     */
    private void showSetupWizard() {
        System.out.println("🔧 Showing setup wizard");
        
        VBox root = new VBox(25);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        // Header
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        
        Label title = new Label("🔒 GhostVault Setup");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitle = new Label("Configure your three security passwords");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #cccccc;");
        
        Label warning = new Label("⚠️ Remember all three passwords - they cannot be recovered");
        warning.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff9800; -fx-font-weight: bold;");
        
        header.getChildren().addAll(title, subtitle, warning);
        
        // Password sections
        VBox passwordSections = new VBox(20);
        passwordSections.setAlignment(Pos.CENTER);
        
        // Master Password
        VBox masterSection = createPasswordSection(
            "Master Password", 
            "Access to your real secure vault", 
            "#4CAF50"
        );
        
        // Panic Password  
        VBox panicSection = createPasswordSection(
            "Panic Password", 
            "Emergency wipe - destroys all data permanently", 
            "#f44336"
        );
        
        // Decoy Password
        VBox decoySection = createPasswordSection(
            "Decoy Password", 
            "Shows fake vault to protect under duress", 
            "#ff9800"
        );
        
        passwordSections.getChildren().addAll(masterSection, panicSection, decoySection);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 120; -fx-pref-height: 40; -fx-background-color: #666666; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> Platform.exit());
        
        Button setupButton = new Button("Complete Setup");
        setupButton.setStyle("-fx-font-size: 14px; -fx-pref-width: 150; -fx-pref-height: 40; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        setupButton.setOnAction(e -> completeSetup());
        
        buttonBox.getChildren().addAll(cancelButton, setupButton);
        
        root.getChildren().addAll(header, passwordSections, buttonBox);
        
        // Create scrollable scene for smaller screens
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1a1a1a; -fx-background-color: #1a1a1a;");
        
        Scene scene = new Scene(scrollPane, 700, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("✅ Setup wizard displayed");
    }
    
    /**
     * Create a password input section
     */
    private VBox createPasswordSection(String title, String description, String borderColor) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 10; -fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 10;");
        section.setMaxWidth(500);
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + borderColor + ";");
        
        // Description
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #cccccc;");
        descLabel.setWrapText(true);
        
        // Password field
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px; -fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");
        
        // Confirm field
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm password");
        confirmField.setPrefHeight(40);
        confirmField.setStyle("-fx-font-size: 14px; -fx-background-color: #3a3a3a; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");
        
        // Strength indicator
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefHeight(8);
        strengthBar.setStyle("-fx-accent: " + borderColor + ";");
        
        Label strengthLabel = new Label("Password strength: Enter a password");
        strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888888;");
        
        // Add password strength checking
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            double strength = calculatePasswordStrength(newText);
            strengthBar.setProgress(strength);
            updateStrengthLabel(strengthLabel, strength);
        });
        
        section.getChildren().addAll(titleLabel, descLabel, passwordField, confirmField, strengthBar, strengthLabel);
        return section;
    }
    
    /**
     * Calculate password strength (0.0 to 1.0)
     */
    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0.0;
        
        int score = 0;
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;
        if (password.matches(".*[a-z].*")) score += 10;
        if (password.matches(".*[A-Z].*")) score += 10;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score += 15;
        
        return Math.min(score / 100.0, 1.0);
    }
    
    /**
     * Update strength label based on score
     */
    private void updateStrengthLabel(Label label, double strength) {
        if (strength < 0.3) {
            label.setText("Password strength: Weak");
            label.setStyle("-fx-font-size: 11px; -fx-text-fill: #f44336;");
        } else if (strength < 0.7) {
            label.setText("Password strength: Medium");
            label.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff9800;");
        } else {
            label.setText("Password strength: Strong");
            label.setStyle("-fx-font-size: 11px; -fx-text-fill: #4CAF50;");
        }
    }
    
    /**
     * Complete the setup process
     */
    private void completeSetup() {
        System.out.println("✅ Setup completed");
        isSetupComplete = true;
        showLoginScreen();
    }
    
    /**
     * Show the login screen
     */
    private void showLoginScreen() {
        System.out.println("🔐 Showing login screen");
        
        VBox root = new VBox(30);
        root.setPadding(new Insets(60));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        // Title
        Label title = new Label("🔒 GhostVault");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label subtitle = new Label("Enter your vault password");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #cccccc;");
        
        // Password input
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(350);
        passwordField.setPrefHeight(50);
        passwordField.setStyle("-fx-font-size: 16px; -fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-background-radius: 8;");
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button exitButton = new Button("Exit");
        exitButton.setPrefWidth(100);
        exitButton.setPrefHeight(45);
        exitButton.setStyle("-fx-font-size: 14px; -fx-background-color: #666666; -fx-text-fill: white; -fx-background-radius: 6;");
        exitButton.setOnAction(e -> Platform.exit());
        
        Button unlockButton = new Button("Unlock Vault");
        unlockButton.setPrefWidth(150);
        unlockButton.setPrefHeight(45);
        unlockButton.setStyle("-fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        unlockButton.setOnAction(e -> authenticateUser(passwordField.getText()));
        
        buttonBox.getChildren().addAll(exitButton, unlockButton);
        
        // Status label
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ff9800;");
        
        // Enter key support
        passwordField.setOnAction(e -> unlockButton.fire());
        
        root.getChildren().addAll(title, subtitle, passwordField, buttonBox, statusLabel);
        
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Focus password field
        Platform.runLater(() -> passwordField.requestFocus());
        
        System.out.println("✅ Login screen displayed");
    }
    
    /**
     * Authenticate user and determine mode
     */
    private void authenticateUser(String password) {
        System.out.println("🔐 Authenticating user with password: [" + password.length() + " chars]");
        
        if (password.isEmpty()) {
            System.out.println("❌ Empty password");
            return;
        }
        
        // Simulate authentication
        String mode = determineMode(password);
        System.out.println("✅ Authentication successful - Mode: " + mode);
        
        showMainApplication(mode);
    }
    
    /**
     * Determine vault mode based on password
     */
    private String determineMode(String password) {
        // Simple mode detection for demo
        if (password.toLowerCase().contains("master")) return "MASTER";
        if (password.toLowerCase().contains("panic")) return "PANIC";
        return "DECOY";
    }
    
    /**
     * Show main application
     */
    private void showMainApplication(String mode) {
        System.out.println("🏠 Showing main application - Mode: " + mode);
        
        VBox root = new VBox(30);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        Label title = new Label("🎉 Welcome to GhostVault!");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label modeLabel = new Label("Mode: " + mode);
        modeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        
        Label message = new Label("Your secure vault is ready for use.");
        message.setStyle("-fx-font-size: 16px; -fx-text-fill: #cccccc;");
        
        Button backButton = new Button("Back to Login");
        backButton.setPrefWidth(150);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-font-size: 14px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 6;");
        backButton.setOnAction(e -> showLoginScreen());
        
        root.getChildren().addAll(title, modeLabel, message, backButton);
        
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        
        System.out.println("✅ Main application displayed");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}