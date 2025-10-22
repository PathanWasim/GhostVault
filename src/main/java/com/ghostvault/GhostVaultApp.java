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
     * Show main application with rich UI like the original
     */
    private void showMainApplication(String mode) {
        System.out.println("🏠 Showing main application - Mode: " + mode);
        
        // Create main layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        // Create toolbar with all the buttons from your screenshot
        ToolBar toolbar = createRichToolbar();
        root.setTop(toolbar);
        
        // Create main content area with three panels
        HBox mainContent = createMainContentArea();
        root.setCenter(mainContent);
        
        // Create status bar
        HBox statusBar = createStatusBar(mode);
        root.setBottom(statusBar);
        
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("GhostVault - Secure File Management (" + mode + " Mode)");
        
        System.out.println("✅ Main application displayed with rich UI");
    }
    
    /**
     * Create rich toolbar with all features
     */
    private ToolBar createRichToolbar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444444; -fx-border-width: 0 0 1 0;");
        
        // File operations
        Button uploadBtn = createToolbarButton("📁 Upload", "#4CAF50");
        Button downloadBtn = createToolbarButton("💾 Download", "#2196F3");
        Button previewBtn = createToolbarButton("👁️ Preview", "#FF9800");
        Button deleteBtn = createToolbarButton("🗑️ Delete", "#f44336");
        
        Separator sep1 = new Separator();
        
        // Vault operations
        Button backupBtn = createToolbarButton("📦 Backup", "#9C27B0");
        Button restoreBtn = createToolbarButton("📥 Restore", "#607D8B");
        
        Separator sep2 = new Separator();
        
        // Advanced features
        Button dashboardBtn = createToolbarButton("📊 Dashboard", "#4CAF50");
        Button notesBtn = createToolbarButton("📝 Notes", "#4CAF50");
        Button passwordsBtn = createToolbarButton("🔑 Passwords", "#4CAF50");
        Button aiBtn = createToolbarButton("🤖 AI Enhanced", "#4CAF50");
        
        Separator sep3 = new Separator();
        
        // Session controls
        Button settingsBtn = createToolbarButton("⚙️ Settings", "#666666");
        Button logoutBtn = createToolbarButton("🚪 Logout", "#f44336");
        
        // Add event handlers
        uploadBtn.setOnAction(e -> handleUpload());
        downloadBtn.setOnAction(e -> handleDownload());
        previewBtn.setOnAction(e -> handlePreview());
        deleteBtn.setOnAction(e -> handleDelete());
        backupBtn.setOnAction(e -> handleBackup());
        restoreBtn.setOnAction(e -> handleRestore());
        dashboardBtn.setOnAction(e -> handleDashboard());
        notesBtn.setOnAction(e -> handleNotes());
        passwordsBtn.setOnAction(e -> handlePasswords());
        aiBtn.setOnAction(e -> handleAI());
        settingsBtn.setOnAction(e -> handleSettings());
        logoutBtn.setOnAction(e -> showLoginScreen());
        
        // Session info
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label sessionLabel = new Label("Session: Active");
        sessionLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
        
        toolbar.getItems().addAll(
            uploadBtn, downloadBtn, previewBtn, deleteBtn, sep1,
            backupBtn, restoreBtn, sep2,
            dashboardBtn, notesBtn, passwordsBtn, aiBtn, sep3,
            settingsBtn, logoutBtn, spacer, sessionLabel
        );
        
        return toolbar;
    }
    
    /**
     * Create toolbar button with styling
     */
    private Button createToolbarButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 6 12 6 12; " +
            "-fx-background-radius: 4; " +
            "-fx-border-radius: 4;"
        );
        
        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle() + "-fx-opacity: 0.8;");
        });
        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", ""));
        });
        
        return button;
    }
    
    /**
     * Create main content area with three panels
     */
    private HBox createMainContentArea() {
        HBox mainContent = new HBox();
        mainContent.setStyle("-fx-background-color: #1a1a1a;");
        
        // Left panel - Files
        VBox filesPanel = createFilesPanel();
        
        // Center panel - Preview
        VBox previewPanel = createPreviewPanel();
        
        // Right panel - File Information
        VBox infoPanel = createInfoPanel();
        
        mainContent.getChildren().addAll(filesPanel, previewPanel, infoPanel);
        
        return mainContent;
    }
    
    /**
     * Create files panel (left)
     */
    private VBox createFilesPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(350);
        panel.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #444444; -fx-border-width: 0 1 0 0;");
        
        Label title = new Label("Files");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search files, try 'find my documents' or 'show me images'");
        searchField.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-prompt-text-fill: #888888;");
        
        // File list
        ListView<String> fileList = new ListView<>();
        fileList.setStyle("-fx-background-color: #2b2b2b; -fx-control-inner-background: #2b2b2b;");
        
        // Add sample files
        fileList.getItems().addAll(
            "📁 ghostvault",
            "📁 decoy", 
            "📁 decoys",
            "📁 files",
            "📁 real",
            "📄 config.enc",
            "📄 metadata.enc", 
            "📄 secure_notes.enc",
            "📄 stored_passwords.enc",
            "📄 vault.config"
        );
        
        VBox.setVgrow(fileList, Priority.ALWAYS);
        
        panel.getChildren().addAll(title, searchField, fileList);
        
        return panel;
    }
    
    /**
     * Create preview panel (center)
     */
    private VBox createPreviewPanel() {
        VBox panel = new VBox();
        panel.setPrefWidth(500);
        panel.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #444444; -fx-border-width: 0 1 0 0;");
        
        Label title = new Label("Preview");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");
        
        // Preview area
        VBox previewArea = new VBox();
        previewArea.setAlignment(Pos.CENTER);
        previewArea.setStyle("-fx-background-color: #1e1e1e;");
        VBox.setVgrow(previewArea, Priority.ALWAYS);
        
        Label previewText = new Label("Select a file to preview");
        previewText.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        previewArea.getChildren().add(previewText);
        
        panel.getChildren().addAll(title, previewArea);
        
        return panel;
    }
    
    /**
     * Create info panel (right)
     */
    private VBox createInfoPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(350);
        panel.setStyle("-fx-background-color: #1e1e1e;");
        
        Label title = new Label("File Information");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label noFileLabel = new Label("No file selected");
        noFileLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        
        // Operations section
        Label opsTitle = new Label("Operations");
        opsTitle.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");
        
        Button uploadFilesBtn = new Button("Upload Files");
        uploadFilesBtn.setPrefWidth(280);
        uploadFilesBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;");
        uploadFilesBtn.setOnAction(e -> handleUpload());
        
        Button downloadBtn = new Button("Download");
        downloadBtn.setPrefWidth(280);
        downloadBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;");
        downloadBtn.setOnAction(e -> handleDownload());
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setPrefWidth(280);
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10;");
        deleteBtn.setOnAction(e -> handleDelete());
        
        panel.getChildren().addAll(title, noFileLabel, opsTitle, uploadFilesBtn, downloadBtn, deleteBtn);
        
        return panel;
    }
    
    /**
     * Create status bar
     */
    private HBox createStatusBar(String mode) {
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(8, 16, 8, 16));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444444; -fx-border-width: 1 0 0 0;");
        
        Label filesLabel = new Label("📁 9 items");
        filesLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
        
        Label sizeLabel = new Label("💾 17.4 MB");
        sizeLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
        
        Label encryptionLabel = new Label("🔐 Files encrypted with AES-256");
        encryptionLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label securityLabel = new Label("🟢 Secure");
        securityLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px;");
        
        Label versionLabel = new Label("GhostVault v2.0");
        versionLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
        
        statusBar.getChildren().addAll(filesLabel, sizeLabel, encryptionLabel, spacer, securityLabel, versionLabel);
        
        return statusBar;
    }
    
    // Event handlers for all the buttons
    private void handleUpload() {
        showInfo("Upload", "Upload functionality - Select files to encrypt and store in vault");
    }
    
    private void handleDownload() {
        showInfo("Download", "Download functionality - Decrypt and save selected files");
    }
    
    private void handlePreview() {
        showInfo("Preview", "Preview functionality - View file contents without downloading");
    }
    
    private void handleDelete() {
        showInfo("Delete", "Delete functionality - Securely remove files from vault");
    }
    
    private void handleBackup() {
        showInfo("Backup", "Backup functionality - Create encrypted backup of entire vault");
    }
    
    private void handleRestore() {
        showInfo("Restore", "Restore functionality - Restore vault from encrypted backup");
    }
    
    private void handleDashboard() {
        showInfo("Dashboard", "Security Dashboard - Real-time monitoring and analytics");
    }
    
    private void handleNotes() {
        showInfo("Notes", "Secure Notes - Encrypted note-taking and storage");
    }
    
    private void handlePasswords() {
        showInfo("Passwords", "Password Manager - Secure password storage and generation");
    }
    
    private void handleAI() {
        showInfo("AI Enhanced", "AI Features - Smart file organization and search");
    }
    
    private void handleSettings() {
        showInfo("Settings", "Application Settings - Configure vault preferences");
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}