package com.ghostvault.ui;

import com.ghostvault.config.AppConfig;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.util.FileUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for initial vault setup and password configuration
 * Handles first-run setup with password creation and validation
 */
public class InitialSetupController {
    
    private Stage primaryStage;
    private PasswordManager passwordManager;
    private PasswordStrengthMeter strengthMeter;
    
    // UI Components
    private PasswordField masterPasswordField;
    private PasswordField confirmMasterPasswordField;
    private PasswordField panicPasswordField;
    private PasswordField confirmPanicPasswordField;
    private PasswordField decoyPasswordField;
    private PasswordField confirmDecoyPasswordField;
    
    private Label masterStrengthLabel;
    private Label panicStrengthLabel;
    private Label decoyStrengthLabel;
    
    private Button setupButton;
    private Label statusLabel;
    private ProgressBar setupProgress;
    
    public InitialSetupController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.passwordManager = new PasswordManager();
        this.strengthMeter = new PasswordStrengthMeter();
    }
    
    /**
     * Show the initial setup dialog
     */
    public void showSetupDialog() {
        Stage setupStage = new Stage();
        setupStage.setTitle("GhostVault - Initial Setup");
        setupStage.setResizable(false);
        
        VBox root = createSetupLayout();
        Scene scene = new Scene(root, 500, 700);
        
        // Apply styling
        scene.getStylesheets().add(getClass().getResource("/styles/setup.css").toExternalForm());
        
        setupStage.setScene(scene);
        setupStage.setOnCloseRequest(e -> {
            // Prevent closing without completing setup
            e.consume();
            showExitConfirmation(setupStage);
        });
        
        setupStage.showAndWait();
    }
    
    /**
     * Create the setup layout
     */
    private VBox createSetupLayout() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Title
        Label titleLabel = new Label("Welcome to GhostVault");
        titleLabel.getStyleClass().add("title-label");
        
        Label subtitleLabel = new Label("Secure your files with military-grade encryption");
        subtitleLabel.getStyleClass().add("subtitle-label");
        
        // Instructions
        Label instructionsLabel = new Label(
            "Create three different passwords:\\n" +
            "• Master Password: Access your real files\\n" +
            "• Panic Password: Instantly destroy all data\\n" +
            "• Decoy Password: Show fake files to hide real data"
        );
        instructionsLabel.getStyleClass().add("instructions-label");
        instructionsLabel.setWrapText(true);
        
        // Password fields
        VBox passwordSection = createPasswordSection();
        
        // Setup button and progress
        VBox actionSection = createActionSection();
        
        root.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Separator(),
            instructionsLabel,
            passwordSection,
            actionSection
        );
        
        return root;
    }
    
    /**
     * Create password input section
     */
    private VBox createPasswordSection() {
        VBox section = new VBox(15);
        
        // Master password
        section.getChildren().addAll(createPasswordGroup(
            "Master Password",
            "Enter your master password to access real files",
            masterPasswordField = new PasswordField(),
            confirmMasterPasswordField = new PasswordField(),
            masterStrengthLabel = new Label()
        ));
        
        // Panic password
        section.getChildren().addAll(createPasswordGroup(
            "Panic Password",
            "Enter panic password (will destroy all data when used)",
            panicPasswordField = new PasswordField(),
            confirmPanicPasswordField = new PasswordField(),
            panicStrengthLabel = new Label()
        ));
        
        // Decoy password
        section.getChildren().addAll(createPasswordGroup(
            "Decoy Password",
            "Enter decoy password (will show fake files)",
            decoyPasswordField = new PasswordField(),
            confirmDecoyPasswordField = new PasswordField(),
            decoyStrengthLabel = new Label()
        ));
        
        // Add password validation listeners
        setupPasswordValidation();
        
        return section;
    }
    
    /**
     * Create a password input group
     */
    private VBox createPasswordGroup(String title, String description, 
                                   PasswordField passwordField, PasswordField confirmField,
                                   Label strengthLabel) {
        VBox group = new VBox(8);
        group.getStyleClass().add("password-group");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("password-title");
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("password-description");
        descLabel.setWrapText(true);
        
        passwordField.setPromptText("Enter " + title.toLowerCase());
        confirmField.setPromptText("Confirm " + title.toLowerCase());
        
        strengthLabel.getStyleClass().add("strength-label");
        
        group.getChildren().addAll(titleLabel, descLabel, passwordField, confirmField, strengthLabel);
        
        return group;
    }
    
    /**
     * Create action section with setup button and progress
     */
    private VBox createActionSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);
        
        setupButton = new Button("Create Vault");
        setupButton.getStyleClass().add("setup-button");
        setupButton.setDisable(true);
        setupButton.setOnAction(e -> performSetup());
        
        setupProgress = new ProgressBar();
        setupProgress.setVisible(false);
        setupProgress.setPrefWidth(300);
        
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        
        section.getChildren().addAll(setupButton, setupProgress, statusLabel);
        
        return section;
    }
    
    /**
     * Setup password validation listeners
     */
    private void setupPasswordValidation() {
        // Master password validation
        masterPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal, masterStrengthLabel);
            validateAllPasswords();
        });
        
        confirmMasterPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAllPasswords();
        });
        
        // Panic password validation
        panicPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal, panicStrengthLabel);
            validateAllPasswords();
        });
        
        confirmPanicPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAllPasswords();
        });
        
        // Decoy password validation
        decoyPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePasswordStrength(newVal, decoyStrengthLabel);
            validateAllPasswords();
        });
        
        confirmDecoyPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAllPasswords();
        });
    }
    
    /**
     * Update password strength indicator
     */
    private void updatePasswordStrength(String password, Label strengthLabel) {
        if (password.isEmpty()) {
            strengthLabel.setText("");
            return;
        }
        
        PasswordStrengthMeter.StrengthResult result = strengthMeter.checkStrength(password);
        strengthLabel.setText(result.getDescription());
        
        // Apply styling based on strength
        strengthLabel.getStyleClass().removeAll("weak", "fair", "good", "strong", "very-strong");
        switch (result.getStrength()) {
            case WEAK:
                strengthLabel.getStyleClass().add("weak");
                break;
            case FAIR:
                strengthLabel.getStyleClass().add("fair");
                break;
            case GOOD:
                strengthLabel.getStyleClass().add("good");
                break;
            case STRONG:
                strengthLabel.getStyleClass().add("strong");
                break;
            case VERY_STRONG:
                strengthLabel.getStyleClass().add("very-strong");
                break;
        }
    }
    
    /**
     * Validate all passwords and enable/disable setup button
     */
    private void validateAllPasswords() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();
        
        String masterPassword = masterPasswordField.getText();
        String confirmMaster = confirmMasterPasswordField.getText();
        String panicPassword = panicPasswordField.getText();
        String confirmPanic = confirmPanicPasswordField.getText();
        String decoyPassword = decoyPasswordField.getText();
        String confirmDecoy = confirmDecoyPasswordField.getText();
        
        // Check if all fields are filled
        if (masterPassword.isEmpty() || confirmMaster.isEmpty() ||
            panicPassword.isEmpty() || confirmPanic.isEmpty() ||
            decoyPassword.isEmpty() || confirmDecoy.isEmpty()) {
            isValid = false;
        }
        
        // Check password confirmations match
        if (!masterPassword.equals(confirmMaster)) {
            errors.append("Master passwords don't match\\n");
            isValid = false;
        }
        
        if (!panicPassword.equals(confirmPanic)) {
            errors.append("Panic passwords don't match\\n");
            isValid = false;
        }
        
        if (!decoyPassword.equals(confirmDecoy)) {
            errors.append("Decoy passwords don't match\\n");
            isValid = false;
        }
        
        // Check all passwords are different
        if (!masterPassword.isEmpty() && !panicPassword.isEmpty() && masterPassword.equals(panicPassword)) {
            errors.append("Master and panic passwords must be different\\n");
            isValid = false;
        }
        
        if (!masterPassword.isEmpty() && !decoyPassword.isEmpty() && masterPassword.equals(decoyPassword)) {
            errors.append("Master and decoy passwords must be different\\n");
            isValid = false;
        }
        
        if (!panicPassword.isEmpty() && !decoyPassword.isEmpty() && panicPassword.equals(decoyPassword)) {
            errors.append("Panic and decoy passwords must be different\\n");
            isValid = false;
        }
        
        // Check password strength requirements
        if (!masterPassword.isEmpty()) {
            PasswordStrengthMeter.StrengthResult masterStrength = strengthMeter.checkStrength(masterPassword);
            if (masterStrength.getStrength().ordinal() < PasswordStrengthMeter.PasswordStrength.GOOD.ordinal()) {
                errors.append("Master password must be at least 'Good' strength\\n");
                isValid = false;
            }
        }
        
        if (!panicPassword.isEmpty()) {
            PasswordStrengthMeter.StrengthResult panicStrength = strengthMeter.checkStrength(panicPassword);
            if (panicStrength.getStrength().ordinal() < PasswordStrengthMeter.PasswordStrength.GOOD.ordinal()) {
                errors.append("Panic password must be at least 'Good' strength\\n");
                isValid = false;
            }
        }
        
        if (!decoyPassword.isEmpty()) {
            PasswordStrengthMeter.StrengthResult decoyStrength = strengthMeter.checkStrength(decoyPassword);
            if (decoyStrength.getStrength().ordinal() < PasswordStrengthMeter.PasswordStrength.GOOD.ordinal()) {
                errors.append("Decoy password must be at least 'Good' strength\\n");
                isValid = false;
            }
        }
        
        setupButton.setDisable(!isValid);
        
        if (errors.length() > 0) {
            statusLabel.setText(errors.toString().trim());
            statusLabel.getStyleClass().removeAll("success", "error");
            statusLabel.getStyleClass().add("error");
        } else if (isValid) {
            statusLabel.setText("Ready to create vault");
            statusLabel.getStyleClass().removeAll("success", "error");
            statusLabel.getStyleClass().add("success");
        } else {
            statusLabel.setText("Please fill in all password fields");
            statusLabel.getStyleClass().removeAll("success", "error");
        }
    }
    
    /**
     * Perform the vault setup
     */
    private void performSetup() {
        setupButton.setDisable(true);
        setupProgress.setVisible(true);
        statusLabel.setText("Creating secure vault...");
        
        // Run setup in background thread
        Thread setupThread = new Thread(() -> {
            try {
                // Update progress
                Platform.runLater(() -> {
                    setupProgress.setProgress(0.2);
                    statusLabel.setText("Creating vault directory...");
                });
                
                // Create vault directory structure
                createVaultStructure();
                
                Platform.runLater(() -> {
                    setupProgress.setProgress(0.4);
                    statusLabel.setText("Initializing security...");
                });
                
                // Initialize password manager
                String masterPassword = masterPasswordField.getText();
                String panicPassword = panicPasswordField.getText();
                String decoyPassword = decoyPasswordField.getText();
                
                passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
                
                Platform.runLater(() -> {
                    setupProgress.setProgress(0.6);
                    statusLabel.setText("Setting up encryption...");
                });
                
                // Create configuration files
                createConfigurationFiles();
                
                Platform.runLater(() -> {
                    setupProgress.setProgress(0.8);
                    statusLabel.setText("Finalizing setup...");
                });
                
                // Initialize decoy content
                initializeDecoyContent();
                
                Platform.runLater(() -> {
                    setupProgress.setProgress(1.0);
                    statusLabel.setText("Vault created successfully!");
                    statusLabel.getStyleClass().removeAll("error");
                    statusLabel.getStyleClass().add("success");
                    
                    // Show success and close
                    showSetupComplete();
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setupProgress.setVisible(false);
                    setupButton.setDisable(false);
                    statusLabel.setText("Setup failed: " + e.getMessage());
                    statusLabel.getStyleClass().removeAll("success");
                    statusLabel.getStyleClass().add("error");
                });
            }
        });
        
        setupThread.setDaemon(true);
        setupThread.start();
    }
    
    /**
     * Create vault directory structure
     */
    private void createVaultStructure() throws Exception {
        // Create main vault directory
        Path vaultPath = Paths.get(AppConfig.VAULT_DIR);
        Files.createDirectories(vaultPath);
        
        // Create subdirectories
        Files.createDirectories(vaultPath.resolve("files"));
        Files.createDirectories(vaultPath.resolve("metadata"));
        Files.createDirectories(vaultPath.resolve("decoy"));
        Files.createDirectories(vaultPath.resolve("logs"));
        Files.createDirectories(vaultPath.resolve("config"));
        
        // Set secure permissions
        FileUtils.setSecurePermissions(vaultPath);
    }
    
    /**
     * Create configuration files
     */
    private void createConfigurationFiles() throws Exception {
        // Create salt file
        passwordManager.generateAndStoreSalt();
        
        // Create initial configuration
        AppConfig.saveConfiguration();
        
        // Create empty metadata file
        Path metadataPath = Paths.get(AppConfig.METADATA_FILE);
        if (!Files.exists(metadataPath)) {
            Files.createFile(metadataPath);
            FileUtils.setSecurePermissions(metadataPath);
        }
    }
    
    /**
     * Initialize decoy content
     */
    private void initializeDecoyContent() throws Exception {
        // This would be implemented by the DecoyManager
        // For now, just create the decoy directory structure
        Path decoyPath = Paths.get(AppConfig.VAULT_DIR, "decoy");
        Files.createDirectories(decoyPath.resolve("documents"));
        Files.createDirectories(decoyPath.resolve("images"));
        Files.createDirectories(decoyPath.resolve("misc"));
    }
    
    /**
     * Show setup completion dialog
     */
    private void showSetupComplete() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Setup Complete");
        alert.setHeaderText("Vault Created Successfully");
        alert.setContentText(
            "Your secure vault has been created successfully!\\n\\n" +
            "Important Security Notes:\\n" +
            "• Remember all three passwords - they cannot be recovered\\n" +
            "• The panic password will permanently destroy all data\\n" +
            "• The decoy password shows fake files to hide your real data\\n\\n" +
            "Click OK to continue to the login screen."
        );
        
        alert.showAndWait();
        
        // Close setup window and show main application
        Stage setupStage = (Stage) setupButton.getScene().getWindow();
        setupStage.close();
        
        // Launch main application
        launchMainApplication();
    }
    
    /**
     * Show exit confirmation dialog
     */
    private void showExitConfirmation(Stage setupStage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Setup");
        alert.setHeaderText("Setup Not Complete");
        alert.setContentText(
            "You haven't finished setting up your vault yet.\\n" +
            "If you exit now, no vault will be created.\\n\\n" +
            "Are you sure you want to exit?"
        );
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                setupStage.close();
                Platform.exit();
            }
        });
    }
    
    /**
     * Launch the main application after setup
     */
    private void launchMainApplication() {
        try {
            // This would typically launch the main GhostVault application
            // For now, just show a placeholder
            primaryStage.setTitle("GhostVault - Login");
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Check if initial setup is needed
     */
    public static boolean isSetupNeeded() {
        // Check if vault directory exists and has been initialized
        File vaultDir = new File(AppConfig.VAULT_DIR);
        File configFile = new File(AppConfig.CONFIG_FILE);
        File saltFile = new File(AppConfig.SALT_FILE);
        
        return !vaultDir.exists() || !configFile.exists() || !saltFile.exists();
    }
}