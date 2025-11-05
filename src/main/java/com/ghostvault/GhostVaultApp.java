package com.ghostvault;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Clean, simple GhostVault application with proper three-password system
 */
public class GhostVaultApp extends Application {
    
    private Stage primaryStage;
    private boolean isSetupComplete;
    private com.ghostvault.ui.SystemTrayManager systemTrayManager;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        System.out.println("🚀 Starting GhostVault...");
        
        // Setup window
        primaryStage.setTitle("GhostVault - Secure File Management");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
        
        // Initialize system tray if supported
        if (com.ghostvault.ui.SystemTrayManager.isSystemTraySupported()) {
            systemTrayManager = new com.ghostvault.ui.SystemTrayManager(primaryStage);
            if (systemTrayManager.initializeSystemTray()) {
                System.out.println("✅ System tray initialized");
                
                // Handle window close to minimize to tray instead of exit
                primaryStage.setOnCloseRequest(e -> {
                    e.consume(); // Prevent default close
                    systemTrayManager.minimizeToTray();
                });
            }
        }
        
        // Initialize notification system
        com.ghostvault.ui.components.NotificationSystem.initialize(primaryStage);
        
        // Check if we're being forced to show login (e.g., after logout)
        boolean forceLogin = "true".equals(System.getProperty("ghostvault.force.login"));
        if (forceLogin) {
            System.out.println("🔓 Forced login mode - showing login screen");
            System.clearProperty("ghostvault.force.login"); // Clear the property
            showLoginScreen();
            return;
        }
        
        // Check if setup is needed by checking if user has set custom passwords
        try {
            com.ghostvault.security.SecureAuthenticationManager authManager = 
                new com.ghostvault.security.SecureAuthenticationManager();
            isSetupComplete = authManager.isSetupComplete();
            System.out.println("🔍 Setup status check: isSetupComplete = " + isSetupComplete);
        } catch (Exception e) {
            System.err.println("Error checking setup status: " + e.getMessage());
            isSetupComplete = false;
        }
        
        if (!isSetupComplete) {
            System.out.println("🔧 Setup required - showing setup wizard");
            showFallbackSetupWizard();
        } else {
            System.out.println("✅ Setup complete - showing login screen");
            showLoginScreen();
        }
    }
    
    /**
     * Show the modern setup wizard with single password entry
     */
    private void showModernSetupWizard() {
        System.out.println("🔧 Showing setup wizard");
        
        try {
            com.ghostvault.ui.controllers.InitialSetupController setupController = 
                new com.ghostvault.ui.controllers.InitialSetupController(primaryStage);
            
            setupController.setOnSetupComplete(success -> {
                if (success) {
                    isSetupComplete = true;
                    showLoginScreen();
                } else {
                    System.out.println("Setup cancelled");
                    Platform.exit();
                }
            });
            
            setupController.show();
            System.out.println("✅ Setup wizard displayed");
            
        } catch (Exception e) {
            System.err.println("Error showing setup wizard: " + e.getMessage());
            e.printStackTrace();
            showFallbackSetupWizard();
        }
    }
    
    // Store password fields for extraction
    private javafx.scene.control.PasswordField masterPasswordField;
    private javafx.scene.control.PasswordField masterConfirmField;
    private javafx.scene.control.PasswordField panicPasswordField;
    private javafx.scene.control.PasswordField panicConfirmField;
    private javafx.scene.control.PasswordField decoyPasswordField;
    private javafx.scene.control.PasswordField decoyConfirmField;
    
    /**
     * Fallback setup wizard if modern one fails
     */
    private void showFallbackSetupWizard() {
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
            "#4CAF50",
            0
        );
        
        // Panic Password  
        VBox panicSection = createPasswordSection(
            "Panic Password", 
            "Emergency wipe - destroys all data permanently", 
            "#f44336",
            1
        );
        
        // Decoy Password
        VBox decoySection = createPasswordSection(
            "Decoy Password", 
            "Shows fake vault to protect under duress", 
            "#ff9800",
            2
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
    private VBox createPasswordSection(String title, String description, String borderColor, int sectionIndex) {
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
        
        // Store references to password fields for later extraction
        switch (sectionIndex) {
            case 0: // Master
                masterPasswordField = passwordField;
                masterConfirmField = confirmField;
                break;
            case 1: // Panic
                panicPasswordField = passwordField;
                panicConfirmField = confirmField;
                break;
            case 2: // Decoy
                decoyPasswordField = passwordField;
                decoyConfirmField = confirmField;
                break;
        }
        
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
        
        // For testing - use simple passwords if UI extraction fails
        String masterPassword = extractPasswordFromSection(0);
        String panicPassword = extractPasswordFromSection(1);
        String decoyPassword = extractPasswordFromSection(2);
        
        // If UI extraction failed, show error and don't proceed
        if (masterPassword.isEmpty() || panicPassword.isEmpty() || decoyPassword.isEmpty()) {
            showError("Setup Error", "Please fill in all password fields and ensure passwords match their confirmations.");
            return;
        }
        
        System.out.println("🔍 Extracted passwords - Master: " + (masterPassword.isEmpty() ? "empty" : "set") + 
                          ", Panic: " + (panicPassword.isEmpty() ? "empty" : "set") + 
                          ", Decoy: " + (decoyPassword.isEmpty() ? "empty" : "set"));
        
        // Validate passwords
        if (masterPassword.isEmpty()) {
            showError("Setup Error", "Master password must be set and confirmed.");
            return;
        }
        if (panicPassword.isEmpty()) {
            showError("Setup Error", "Panic password must be set and confirmed.");
            return;
        }
        if (decoyPassword.isEmpty()) {
            showError("Setup Error", "Decoy password must be set and confirmed.");
            return;
        }
        
        if (masterPassword.equals(panicPassword) || masterPassword.equals(decoyPassword) || panicPassword.equals(decoyPassword)) {
            showError("Setup Error", "All three passwords must be different from each other.");
            return;
        }
        
        try {
            System.out.println("🔐 About to save passwords: master=" + masterPassword + ", decoy=" + decoyPassword + ", panic=" + panicPassword);
            
            // Save user passwords using SecureAuthenticationManager
            com.ghostvault.security.SecureAuthenticationManager authManager = 
                new com.ghostvault.security.SecureAuthenticationManager();
            authManager.setUserPasswords(masterPassword, decoyPassword, panicPassword);
            
            // Verify passwords were saved by creating a new instance
            com.ghostvault.security.SecureAuthenticationManager testManager = 
                new com.ghostvault.security.SecureAuthenticationManager();
            boolean setupComplete = testManager.isSetupComplete();
            System.out.println("🔍 Verification: setup complete = " + setupComplete);
            
            isSetupComplete = true;
            showInfo("Setup Complete", 
                "Your vault has been configured successfully!\n\n" +
                "Remember your passwords:\n" +
                "• Master Password: Access your real vault\n" +
                "• Decoy Password: Shows fake vault under duress\n" +
                "• Panic Password: Wipes all data permanently\n\n" +
                "⚠️ These passwords cannot be recovered if forgotten!");
            
            showLoginScreen();
        } catch (Exception e) {
            System.err.println("Error saving setup: " + e.getMessage());
            showError("Setup Error", "Failed to save setup: " + e.getMessage());
        }
    }
    
    /**
     * Extract password from setup section
     */
    private String extractPasswordFromSection(int sectionIndex) {
        String sectionName = (sectionIndex == 0) ? "Master" : (sectionIndex == 1) ? "Panic" : "Decoy";
        
        switch (sectionIndex) {
            case 0: // Master
                if (masterPasswordField != null && masterConfirmField != null) {
                    String password = masterPasswordField.getText();
                    String confirm = masterConfirmField.getText();
                    System.out.println("🔍 " + sectionName + " password check: " + 
                                     (password.isEmpty() ? "empty" : "set") + 
                                     ", confirm: " + (confirm.isEmpty() ? "empty" : "set") + 
                                     ", match: " + password.equals(confirm));
                    if (!password.isEmpty() && password.equals(confirm)) {
                        return password;
                    }
                }
                break;
            case 1: // Panic
                if (panicPasswordField != null && panicConfirmField != null) {
                    String password = panicPasswordField.getText();
                    String confirm = panicConfirmField.getText();
                    System.out.println("🔍 " + sectionName + " password check: " + 
                                     (password.isEmpty() ? "empty" : "set") + 
                                     ", confirm: " + (confirm.isEmpty() ? "empty" : "set") + 
                                     ", match: " + password.equals(confirm));
                    if (!password.isEmpty() && password.equals(confirm)) {
                        return password;
                    }
                }
                break;
            case 2: // Decoy
                if (decoyPasswordField != null && decoyConfirmField != null) {
                    String password = decoyPasswordField.getText();
                    String confirm = decoyConfirmField.getText();
                    System.out.println("🔍 " + sectionName + " password check: " + 
                                     (password.isEmpty() ? "empty" : "set") + 
                                     ", confirm: " + (confirm.isEmpty() ? "empty" : "set") + 
                                     ", match: " + password.equals(confirm));
                    if (!password.isEmpty() && password.equals(confirm)) {
                        return password;
                    }
                }
                break;
        }
        System.out.println("⚠️ " + sectionName + " password extraction failed");
        return "";
    }
    
    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show info dialog
     */
    private void showInfo(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
        
        // Password input container for better width control
        VBox passwordContainer = new VBox();
        passwordContainer.setAlignment(Pos.CENTER);
        passwordContainer.setMaxWidth(400);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(350);
        passwordField.setMaxWidth(350);
        passwordField.setPrefHeight(50);
        passwordField.setStyle("-fx-font-size: 16px; -fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-background-radius: 8;");
        
        passwordContainer.getChildren().add(passwordField);
        
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
        
        root.getChildren().addAll(title, subtitle, passwordContainer, buttonBox, statusLabel);
        
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Focus password field
        Platform.runLater(() -> passwordField.requestFocus());
        
        System.out.println("✅ Login screen displayed");
    }
    
    /**
     * Authenticate user and determine mode using SecureAuthenticationManager
     */
    private void authenticateUser(String password) {
        System.out.println("🔐 Authenticating user with password: [" + password.length() + " chars]");
        
        if (password.isEmpty()) {
            System.out.println("❌ Empty password");
            showAuthenticationError("Password cannot be empty");
            return;
        }
        
        // Use secure authentication manager
        com.ghostvault.security.SecureAuthenticationManager authManager = 
            new com.ghostvault.security.SecureAuthenticationManager();
        
        com.ghostvault.security.AuthenticationResult result = authManager.authenticate(password);
        
        if (!result.isSuccess()) {
            System.out.println("❌ Authentication failed: " + result.getErrorMessage());
            showAuthenticationError(result.getErrorMessage());
            return;
        }
        
        // Handle panic mode specially - wipe system and exit
        if (result.isPanicMode()) {
            System.out.println("🚨 PANIC MODE ACTIVATED - Wiping system");
            executePanicMode();
            return;
        }
        
        System.out.println("✅ Authentication successful - Mode: " + result.getMode());
        showMainApplication(result.getMode().toString(), password);
    }
    
    /**
     * Show authentication error message
     */
    private void showAuthenticationError(String message) {
        // Find the status label in the current scene and update it
        javafx.scene.Scene currentScene = primaryStage.getScene();
        if (currentScene != null && currentScene.getRoot() instanceof javafx.scene.layout.VBox) {
            javafx.scene.layout.VBox root = (javafx.scene.layout.VBox) currentScene.getRoot();
            
            // Find the status label (should be the last child)
            if (!root.getChildren().isEmpty()) {
                javafx.scene.Node lastChild = root.getChildren().get(root.getChildren().size() - 1);
                if (lastChild instanceof javafx.scene.control.Label) {
                    javafx.scene.control.Label statusLabel = (javafx.scene.control.Label) lastChild;
                    statusLabel.setText(message);
                    statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #f44336;");
                }
            }
        }
    }
    
    /**
     * Execute panic mode - wipe all data and exit application
     */
    private void executePanicMode() {
        System.out.println("🚨 Executing panic mode - wiping all vault data");
        
        try {
            // Delete vault directories
            java.nio.file.Path vaultPath = java.nio.file.Paths.get(System.getProperty("user.home"), ".ghostvault");
            if (java.nio.file.Files.exists(vaultPath)) {
                deleteDirectoryRecursively(vaultPath.toFile());
                System.out.println("✅ Vault directory deleted");
            }
            
            // Delete any other application data
            java.nio.file.Path configPath = java.nio.file.Paths.get(System.getProperty("user.home"), ".ghostvault-config");
            if (java.nio.file.Files.exists(configPath)) {
                deleteDirectoryRecursively(configPath.toFile());
                System.out.println("✅ Configuration directory deleted");
            }
            
            System.out.println("🚨 PANIC MODE COMPLETE - All data wiped");
            
        } catch (Exception e) {
            System.err.println("⚠️ Error during panic mode wipe: " + e.getMessage());
            // Continue with exit even if wipe fails partially
        }
        
        // Exit application immediately
        Platform.exit();
        System.exit(0);
    }
    
    /**
     * Recursively delete directory and all contents
     */
    private void deleteDirectoryRecursively(java.io.File directory) {
        if (directory.exists()) {
            java.io.File[] files = directory.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryRecursively(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
    
    /**
     * Show main application using the original FXML-based UI
     */
    private void showMainApplication(String mode, String password) {
        System.out.println("🏠 Showing main application - Mode: " + mode);
        
        try {
            // Load the original FXML-based UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vault_main.fxml"));
            Parent root = loader.load();
            
            // Get the controller and initialize it properly
            com.ghostvault.ui.VaultMainController vaultController = loader.getController();
            
            // Initialize with proper components based on mode
            if (mode.equals("DECOY")) {
                // Initialize decoy mode with proper paths
                java.nio.file.Path realVaultPath = java.nio.file.Paths.get(System.getProperty("user.home"), ".ghostvault", "real");
                java.nio.file.Path decoyVaultPath = java.nio.file.Paths.get(System.getProperty("user.home"), ".ghostvault", "decoy");
                
                com.ghostvault.core.DecoyManager decoyManager = new com.ghostvault.core.DecoyManager(realVaultPath, decoyVaultPath);
                vaultController.initializeDecoyMode(decoyManager);
            } else {
                // Initialize master mode with full functionality
                String vaultPath = com.ghostvault.config.AppConfig.getVaultDir();
                
                com.ghostvault.core.FileManager fileManager;
                com.ghostvault.core.MetadataManager metadataManager;
                
                try {
                    fileManager = new com.ghostvault.core.FileManager(vaultPath);
                    metadataManager = new com.ghostvault.core.MetadataManager(vaultPath + "/metadata/metadata.json");
                    
                    // Load existing metadata to restore file list
                    System.out.println("📋 Loading existing metadata...");
                    metadataManager.loadMetadata();
                    System.out.println("✅ Metadata loaded successfully");
                    
                } catch (Exception e) {
                    System.err.println("❌ Failed to initialize vault: " + e.getMessage());
                    e.printStackTrace();
                    showFallbackMainApplication("ERROR - " + e.getMessage());
                    return;
                }
                
                // Create backup manager with proper dependencies
                com.ghostvault.security.CryptoManager cryptoManager = new com.ghostvault.security.CryptoManager();
                com.ghostvault.audit.AuditManager auditManager = new com.ghostvault.audit.AuditManager();
                com.ghostvault.backup.VaultBackupManager backupManager = new com.ghostvault.backup.VaultBackupManager(
                    cryptoManager, fileManager, metadataManager, auditManager);
                
                // Create encryption key from user password using deterministic key derivation
                javax.crypto.SecretKey encryptionKey = createEncryptionKeyFromPassword(password);
                
                // Initialize the controller
                vaultController.initialize(fileManager, metadataManager, backupManager, encryptionKey);
                
                // Set the session password for proper encryption
                vaultController.setSessionPassword(password);
            }
            
            // Create and show scene with proper styling
            Scene scene = new Scene(root, 1200, 800);
            
            // Apply password manager theme
            com.ghostvault.ui.theme.PasswordManagerTheme.applyPasswordManagerTheme(scene);
            
            primaryStage.setScene(scene);
            primaryStage.setTitle("GhostVault - Secure File Management");
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            
            System.out.println("✅ Main application displayed with original professional UI");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading main application: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple interface
            showFallbackMainApplication(mode);
        }
    }
    
    /**
     * Create encryption key from user password using deterministic key derivation
     */
    private javax.crypto.SecretKey createEncryptionKeyFromPassword(String password) {
        try {
            com.ghostvault.security.EnhancedKeyManager keyManager = new com.ghostvault.security.EnhancedKeyManager();
            return keyManager.deriveKey(password);
        } catch (Exception e) {
            System.err.println("❌ Failed to derive key from password: " + e.getMessage());
            // Fallback to demo key if derivation fails
            return createDemoEncryptionKey();
        }
    }
    
    /**
     * Create a demo encryption key for testing (fallback)
     */
    private javax.crypto.SecretKey createDemoEncryptionKey() {
        try {
            javax.crypto.KeyGenerator keyGen = javax.crypto.KeyGenerator.getInstance("AES");
            keyGen.init(256);
            return keyGen.generateKey();
        } catch (Exception e) {
            System.err.println("❌ Error creating demo encryption key: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Fallback main application if FXML loading fails
     */
    private void showFallbackMainApplication(String mode) {
        System.out.println("🔄 Showing fallback main application...");
        
        VBox root = new VBox(30);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        Label title = new Label("🎉 Welcome to GhostVault!");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label modeLabel = new Label("Mode: " + mode);
        modeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        
        Label message = new Label("Loading professional interface...");
        message.setStyle("-fx-font-size: 16px; -fx-text-fill: #cccccc;");
        
        Button backButton = new Button("Back to Login");
        backButton.setPrefWidth(150);
        backButton.setPrefHeight(40);
        backButton.setStyle("-fx-font-size: 14px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 6;");
        backButton.setOnAction(e -> showLoginScreen());
        
        root.getChildren().addAll(title, modeLabel, message, backButton);
        
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        
        System.out.println("✅ Fallback application displayed");
    }
    

    
    public static void main(String[] args) {
        launch(args);
    }
}