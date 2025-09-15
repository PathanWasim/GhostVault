package com.ghostvault.ui;

import com.ghostvault.config.AppConfig;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.security.PasswordManager;
import com.ghostvault.util.FileUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

/**
 * Controller for initial vault setup and password configuration
 */
public class InitialSetupController {
    
    private Stage setupStage;
    private PasswordField masterPasswordField;
    private PasswordField panicPasswordField;
    private PasswordField decoyPasswordField;
    private PasswordField confirmMasterField;
    private PasswordField confirmPanicField;
    private PasswordField confirmDecoyField;
    
    private ProgressBar masterStrengthBar;
    private ProgressBar panicStrengthBar;
    private ProgressBar decoyStrengthBar;
    
    private Label masterStrengthLabel;
    private Label panicStrengthLabel;
    private Label decoyStrengthLabel;
    
    private Label masterFeedbackLabel;
    private Label panicFeedbackLabel;
    private Label decoyFeedbackLabel;
    
    private Button setupButton;
    private ProgressIndicator setupProgress;
    private Label setupStatusLabel;
    
    private boolean setupCompleted = false;
    
    /**
     * Show the initial setup dialog
     */
    public boolean showSetupDialog(Stage parentStage) {
        setupStage = new Stage();
        setupStage.initModality(Modality.APPLICATION_MODAL);
        setupStage.initOwner(parentStage);
        setupStage.setTitle("GhostVault - Initial Setup");
        setupStage.setResizable(false);
        
        VBox root = createSetupUI();
        Scene scene = new Scene(root, 600, 700);
        setupStage.setScene(scene);
        
        setupStage.showAndWait();
        
        return setupCompleted;
    }
    
    /**
     * Create the setup UI
     */
    private VBox createSetupUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Title
        Label titleLabel = new Label("Welcome to GhostVault");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        
        Label subtitleLabel = new Label("Secure your files with military-grade encryption");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.GRAY);
        
        // Instructions
        Label instructionsLabel = new Label(
            "Set up three different passwords for your vault:\n" +
            "• Master Password: Access your real files\n" +
            "• Panic Password: Instantly destroy all data\n" +
            "• Decoy Password: Show fake files to hide real content"
        );
        instructionsLabel.setWrapText(true);
        instructionsLabel.setFont(Font.font("System", 12));
        
        // Password requirements
        Label requirementsLabel = new Label(PasswordManager.getPasswordRequirements());
        requirementsLabel.setWrapText(true);
        requirementsLabel.setFont(Font.font("System", 10));
        requirementsLabel.setTextFill(Color.DARKBLUE);
        
        // Password fields
        VBox passwordSection = createPasswordSection();
        
        // Setup button and progress
        HBox buttonSection = createButtonSection();
        
        root.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Separator(),
            instructionsLabel,
            requirementsLabel,
            passwordSection,
            buttonSection
        );
        
        return root;
    }
    
    /**
     * Create password input section
     */
    private VBox createPasswordSection() {
        VBox section = new VBox(15);
        
        // Master password
        VBox masterSection = createPasswordInputGroup(
            "Master Password",
            "Enter your master password",
            "Confirm master password"
        );
        masterPasswordField = (PasswordField) ((VBox) masterSection.getChildren().get(1)).getChildren().get(0);
        confirmMasterField = (PasswordField) ((VBox) masterSection.getChildren().get(2)).getChildren().get(0);
        masterStrengthBar = (ProgressBar) ((HBox) masterSection.getChildren().get(3)).getChildren().get(0);
        masterStrengthLabel = (Label) ((HBox) masterSection.getChildren().get(3)).getChildren().get(1);
        masterFeedbackLabel = (Label) masterSection.getChildren().get(4);
        
        // Panic password
        VBox panicSection = createPasswordInputGroup(
            "Panic Password",
            "Enter your panic password",
            "Confirm panic password"
        );
        panicPasswordField = (PasswordField) ((VBox) panicSection.getChildren().get(1)).getChildren().get(0);
        confirmPanicField = (PasswordField) ((VBox) panicSection.getChildren().get(2)).getChildren().get(0);
        panicStrengthBar = (ProgressBar) ((HBox) panicSection.getChildren().get(3)).getChildren().get(0);
        panicStrengthLabel = (Label) ((HBox) panicSection.getChildren().get(3)).getChildren().get(1);
        panicFeedbackLabel = (Label) panicSection.getChildren().get(4);
        
        // Decoy password
        VBox decoySection = createPasswordInputGroup(
            "Decoy Password",
            "Enter your decoy password",
            "Confirm decoy password"
        );
        decoyPasswordField = (PasswordField) ((VBox) decoySection.getChildren().get(1)).getChildren().get(0);
        confirmDecoyField = (PasswordField) ((VBox) decoySection.getChildren().get(2)).getChildren().get(0);
        decoyStrengthBar = (ProgressBar) ((HBox) decoySection.getChildren().get(3)).getChildren().get(0);
        decoyStrengthLabel = (Label) ((HBox) decoySection.getChildren().get(3)).getChildren().get(1);
        decoyFeedbackLabel = (Label) decoySection.getChildren().get(4);
        
        // Add real-time password strength monitoring
        setupPasswordStrengthMonitoring();
        
        section.getChildren().addAll(masterSection, panicSection, decoySection);
        
        return section;
    }
    
    /**
     * Create password input group with strength meter
     */
    private VBox createPasswordInputGroup(String title, String passwordPrompt, String confirmPrompt) {
        VBox group = new VBox(5);
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Password field
        VBox passwordBox = new VBox(3);
        Label passwordLabel = new Label(passwordPrompt);
        PasswordField passwordField = new PasswordField();
        passwordField.setPrefWidth(400);
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // Confirm field
        VBox confirmBox = new VBox(3);
        Label confirmLabel = new Label(confirmPrompt);
        PasswordField confirmField = new PasswordField();
        confirmField.setPrefWidth(400);
        confirmBox.getChildren().addAll(confirmLabel, confirmField);
        
        // Strength meter
        HBox strengthBox = new HBox(10);
        strengthBox.setAlignment(Pos.CENTER_LEFT);
        ProgressBar strengthBar = new ProgressBar(0);
        strengthBar.setPrefWidth(200);
        Label strengthLabel = new Label("");
        strengthBox.getChildren().addAll(strengthBar, strengthLabel);
        
        // Feedback label
        Label feedbackLabel = new Label("");
        feedbackLabel.setWrapText(true);
        feedbackLabel.setFont(Font.font("System", 10));
        feedbackLabel.setTextFill(Color.DARKRED);
        
        group.getChildren().addAll(titleLabel, passwordBox, confirmBox, strengthBox, feedbackLabel);
        
        return group;
    }
    
    /**
     * Setup real-time password strength monitoring
     */
    private void setupPasswordStrengthMonitoring() {
        // Master password monitoring
        masterPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            updatePasswordStrength(newText, masterStrengthBar, masterStrengthLabel, masterFeedbackLabel);
            validateSetupButton();
        });
        
        confirmMasterField.textProperty().addListener((obs, oldText, newText) -> {
            validateSetupButton();
        });
        
        // Panic password monitoring
        panicPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            updatePasswordStrength(newText, panicStrengthBar, panicStrengthLabel, panicFeedbackLabel);
            validateSetupButton();
        });
        
        confirmPanicField.textProperty().addListener((obs, oldText, newText) -> {
            validateSetupButton();
        });
        
        // Decoy password monitoring
        decoyPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            updatePasswordStrength(newText, decoyStrengthBar, decoyStrengthLabel, decoyFeedbackLabel);
            validateSetupButton();
        });
        
        confirmDecoyField.textProperty().addListener((obs, oldText, newText) -> {
            validateSetupButton();
        });
    }
    
    /**
     * Update password strength meter and feedback
     */
    private void updatePasswordStrength(String password, ProgressBar strengthBar, 
                                      Label strengthLabel, Label feedbackLabel) {
        int strength = PasswordManager.getPasswordStrength(password);
        double progress = strength / 5.0;
        
        Platform.runLater(() -> {
            strengthBar.setProgress(progress);
            strengthLabel.setText(PasswordManager.getPasswordStrengthDescription(strength));
            
            // Update strength bar color
            String color = PasswordManager.getPasswordStrengthColor(strength);
            strengthBar.setStyle("-fx-accent: " + color + ";");
            
            // Update feedback
            String feedback = PasswordManager.getPasswordStrengthFeedback(password);
            feedbackLabel.setText(feedback);
            feedbackLabel.setTextFill("Strong password!".equals(feedback) ? Color.GREEN : Color.DARKRED);
        });
    }
    
    /**
     * Create button section
     */
    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);
        
        setupButton = new Button("Create Vault");
        setupButton.setPrefWidth(120);
        setupButton.setDisable(true);
        setupButton.setOnAction(e -> performSetup());
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(120);
        cancelButton.setOnAction(e -> {
            setupCompleted = false;
            setupStage.close();
        });
        
        setupProgress = new ProgressIndicator();
        setupProgress.setVisible(false);
        setupProgress.setPrefSize(30, 30);
        
        setupStatusLabel = new Label("");
        setupStatusLabel.setFont(Font.font("System", 12));
        
        section.getChildren().addAll(setupButton, cancelButton, setupProgress, setupStatusLabel);
        
        return section;
    }
    
    /**
     * Validate setup button state
     */
    private void validateSetupButton() {
        Platform.runLater(() -> {
            boolean valid = isSetupValid();
            setupButton.setDisable(!valid);
        });
    }
    
    /**
     * Check if setup is valid
     */
    private boolean isSetupValid() {
        // Check all passwords are entered
        String masterPwd = masterPasswordField.getText();
        String panicPwd = panicPasswordField.getText();
        String decoyPwd = decoyPasswordField.getText();
        
        if (masterPwd.isEmpty() || panicPwd.isEmpty() || decoyPwd.isEmpty()) {
            return false;
        }
        
        // Check confirmations match
        if (!masterPwd.equals(confirmMasterField.getText()) ||
            !panicPwd.equals(confirmPanicField.getText()) ||
            !decoyPwd.equals(confirmDecoyField.getText())) {
            return false;
        }
        
        // Check password strength requirements
        if (PasswordManager.getPasswordStrength(masterPwd) < AppConfig.PASSWORD_MIN_STRENGTH ||
            PasswordManager.getPasswordStrength(panicPwd) < 3 ||
            PasswordManager.getPasswordStrength(decoyPwd) < 3) {
            return false;
        }
        
        // Check passwords are different
        if (masterPwd.equals(panicPwd) || masterPwd.equals(decoyPwd) || panicPwd.equals(decoyPwd)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Perform vault setup
     */
    private void performSetup() {
        setupButton.setDisable(true);
        setupProgress.setVisible(true);
        setupStatusLabel.setText("Creating vault...");
        
        Task<Boolean> setupTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    // Update status
                    Platform.runLater(() -> setupStatusLabel.setText("Creating directory structure..."));
                    
                    // Create vault directory structure
                    createVaultDirectories();
                    
                    // Update status
                    Platform.runLater(() -> setupStatusLabel.setText("Initializing security..."));
                    
                    // Initialize password manager
                    PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
                    passwordManager.initializePasswords(
                        masterPasswordField.getText(),
                        panicPasswordField.getText(),
                        decoyPasswordField.getText()
                    );
                    
                    // Update status
                    Platform.runLater(() -> setupStatusLabel.setText("Setting up metadata..."));
                    
                    // Initialize metadata manager
                    MetadataManager metadataManager = new MetadataManager(AppConfig.METADATA_FILE);
                    metadataManager.setEncryptionKey(passwordManager.deriveVaultKey(masterPasswordField.getText()));
                    metadataManager.saveMetadata(); // Create empty metadata file
                    
                    // Update status
                    Platform.runLater(() -> setupStatusLabel.setText("Vault created successfully!"));
                    
                    return true;
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        setupStatusLabel.setText("Setup failed: " + e.getMessage());
                        setupStatusLabel.setTextFill(Color.RED);
                    });
                    throw e;
                }
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    setupProgress.setVisible(false);
                    setupStatusLabel.setTextFill(Color.GREEN);
                    
                    // Show success and close after delay
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                        setupCompleted = true;
                        setupStage.close();
                    }));
                    timeline.play();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setupProgress.setVisible(false);
                    setupButton.setDisable(false);
                });
            }
        };
        
        Thread setupThread = new Thread(setupTask);
        setupThread.setDaemon(true);
        setupThread.start();
    }
    
    /**
     * Create vault directory structure
     */
    private void createVaultDirectories() throws Exception {
        // Create main vault directory
        FileUtils.ensureDirectoryExists(AppConfig.VAULT_DIR);
        
        // Create subdirectories
        FileUtils.ensureDirectoryExists(AppConfig.FILES_DIR);
        FileUtils.ensureDirectoryExists(AppConfig.DECOYS_DIR);
        
        // Hide vault directory on Windows
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                File vaultDir = new File(AppConfig.VAULT_DIR);
                Runtime.getRuntime().exec("attrib +H \"" + vaultDir.getAbsolutePath() + "\"");
            } catch (Exception e) {
                // Ignore if hiding fails
            }
        }
    }
    
    /**
     * Check if vault is already initialized
     */
    public static boolean isVaultInitialized() {
        File configFile = new File(AppConfig.CONFIG_FILE);
        File saltFile = new File(AppConfig.SALT_FILE);
        File vaultDir = new File(AppConfig.VAULT_DIR);
        
        return configFile.exists() && saltFile.exists() && vaultDir.exists();
    }
}