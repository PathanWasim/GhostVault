package com.ghostvault.ui;

import com.ghostvault.migration.ComprehensiveMigrationUtility;
import com.ghostvault.security.SecurityValidationSystem;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * UI dialog for security migration operations
 * Provides user-friendly interface for migrating to encrypted storage
 */
public class SecurityMigrationDialog {
    
    private final Stage parentStage;
    private final String vaultPath;
    private Stage dialogStage;
    private ProgressBar progressBar;
    private TextArea logArea;
    private Button migrateButton;
    private Button cancelButton;
    private Label statusLabel;
    
    public SecurityMigrationDialog(Stage parentStage, String vaultPath) {
        this.parentStage = parentStage;
        this.vaultPath = vaultPath;
        createDialog();
    }
    
    /**
     * Show the migration dialog
     */
    public void show() {
        dialogStage.show();
        
        // Perform initial assessment
        performSecurityAssessment();
    }
    
    /**
     * Create the dialog UI
     */
    private void createDialog() {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("Security Migration - GhostVault");
        dialogStage.setResizable(false);
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        
        // Header
        Label headerLabel = new Label("🔒 Security Migration Required");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label descriptionLabel = new Label(
            "Your vault contains unencrypted data that needs to be migrated to secure storage.\n" +
            "This process will encrypt your passwords, files, and metadata using industry-standard encryption."
        );
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(500);
        descriptionLabel.setStyle("-fx-text-alignment: center;");
        
        // Status section
        statusLabel = new Label("Analyzing vault security...");
        statusLabel.setStyle("-fx-font-weight: bold;");
        
        // Progress section
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);
        
        // Log area
        logArea = new TextArea();
        logArea.setPrefRowCount(10);
        logArea.setPrefColumnCount(50);
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        
        ScrollPane logScrollPane = new ScrollPane(logArea);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setPrefHeight(200);
        
        // Password input
        Label passwordLabel = new Label("Enter your master password to encrypt the data:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Master password");
        passwordField.setPrefWidth(300);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        migrateButton = new Button("🔐 Start Migration");
        migrateButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        migrateButton.setPrefWidth(150);
        migrateButton.setDisable(true);
        
        cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        
        Button validateButton = new Button("🔍 Validate Security");
        validateButton.setPrefWidth(150);
        
        buttonBox.getChildren().addAll(validateButton, migrateButton, cancelButton);
        
        // Event handlers
        migrateButton.setOnAction(e -> {
            String password = passwordField.getText();
            if (password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Password Required", "Please enter your master password.");
                return;
            }
            performMigration(password);
        });
        
        cancelButton.setOnAction(e -> dialogStage.close());
        
        validateButton.setOnAction(e -> performSecurityValidation());
        
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            migrateButton.setDisable(newText.trim().isEmpty());
        });
        
        // Layout
        VBox passwordSection = new VBox(5);
        passwordSection.setAlignment(Pos.CENTER);
        passwordSection.getChildren().addAll(passwordLabel, passwordField);
        
        root.getChildren().addAll(
            headerLabel,
            descriptionLabel,
            new Separator(),
            statusLabel,
            progressBar,
            logScrollPane,
            passwordSection,
            buttonBox
        );
        
        Scene scene = new Scene(root, 600, 500);
        dialogStage.setScene(scene);
    }
    
    /**
     * Perform initial security assessment
     */
    private void performSecurityAssessment() {
        Task<ComprehensiveMigrationUtility.MigrationAssessment> task = new Task<>() {
            @Override
            protected ComprehensiveMigrationUtility.MigrationAssessment call() throws Exception {
                ComprehensiveMigrationUtility migrationUtility = new ComprehensiveMigrationUtility(vaultPath);
                return migrationUtility.assessMigrationNeeds();
            }
            
            @Override
            protected void succeeded() {
                ComprehensiveMigrationUtility.MigrationAssessment assessment = getValue();
                Platform.runLater(() -> updateAssessmentUI(assessment));
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Assessment failed");
                    logArea.appendText("Error: " + getException().getMessage() + "\n");
                });
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * Update UI based on assessment results
     */
    private void updateAssessmentUI(ComprehensiveMigrationUtility.MigrationAssessment assessment) {
        if (!assessment.needsAnyMigration()) {
            statusLabel.setText("✅ Vault is already secure");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            migrateButton.setText("✅ Already Secure");
            migrateButton.setDisable(true);
            logArea.appendText("✅ Security Assessment Complete\n");
            logArea.appendText("• Passwords: Encrypted\n");
            logArea.appendText("• Files: Encrypted\n");
            logArea.appendText("• Metadata: Encrypted\n");
            logArea.appendText("\nYour vault is fully secure!\n");
        } else {
            statusLabel.setText("⚠️ Migration required");
            statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            
            logArea.appendText("🔍 Security Assessment Results:\n");
            if (assessment.needsPasswordMigration()) {
                logArea.appendText("❌ Passwords: Plain text (CRITICAL)\n");
            } else {
                logArea.appendText("✅ Passwords: Encrypted\n");
            }
            
            if (assessment.needsFileMigration()) {
                logArea.appendText("❌ Files: " + assessment.getFileStatus().getUnencryptedCount() + " unencrypted\n");
            } else {
                logArea.appendText("✅ Files: All encrypted\n");
            }
            
            if (assessment.needsMetadataMigration()) {
                logArea.appendText("❌ Metadata: Plain text\n");
            } else {
                logArea.appendText("✅ Metadata: Encrypted\n");
            }
            
            logArea.appendText("\n⚠️ Migration is required to secure your data.\n");
        }
    }
    
    /**
     * Perform security validation
     */
    private void performSecurityValidation() {
        Task<SecurityValidationSystem.SecurityValidationReport> task = new Task<>() {
            @Override
            protected SecurityValidationSystem.SecurityValidationReport call() throws Exception {
                SecurityValidationSystem validationSystem = new SecurityValidationSystem(vaultPath);
                return validationSystem.performSecurityValidation();
            }
            
            @Override
            protected void succeeded() {
                SecurityValidationSystem.SecurityValidationReport report = getValue();
                Platform.runLater(() -> displayValidationReport(report));
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    logArea.appendText("❌ Validation failed: " + getException().getMessage() + "\n");
                });
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * Display validation report in log area
     */
    private void displayValidationReport(SecurityValidationSystem.SecurityValidationReport report) {
        logArea.appendText("\n🔍 Security Validation Report:\n");
        logArea.appendText("Overall Security Level: " + report.getOverallLevel() + "\n\n");
        
        for (SecurityValidationSystem.SecurityFinding finding : report.getFindings()) {
            String icon = getSecurityLevelIcon(finding.getLevel());
            logArea.appendText(icon + " " + finding.getCategory() + ": " + finding.getSummary() + "\n");
            
            for (String issue : finding.getIssues()) {
                logArea.appendText("  ❌ " + issue + "\n");
            }
            
            for (String detail : finding.getDetails()) {
                logArea.appendText("  " + detail + "\n");
            }
            
            logArea.appendText("\n");
        }
        
        logArea.appendText("📋 Recommendations:\n");
        for (String recommendation : report.getRecommendations()) {
            logArea.appendText("• " + recommendation + "\n");
        }
        
        logArea.appendText("\n");
    }
    
    /**
     * Perform migration
     */
    private void performMigration(String password) {
        migrateButton.setDisable(true);
        cancelButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate
        
        statusLabel.setText("🔄 Migrating to secure storage...");
        statusLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
        
        logArea.appendText("\n🔄 Starting comprehensive migration...\n");
        
        Task<ComprehensiveMigrationUtility.ComprehensiveMigrationResult> task = new Task<>() {
            @Override
            protected ComprehensiveMigrationUtility.ComprehensiveMigrationResult call() throws Exception {
                ComprehensiveMigrationUtility migrationUtility = new ComprehensiveMigrationUtility(vaultPath);
                return migrationUtility.performComprehensiveMigration(password);
            }
            
            @Override
            protected void succeeded() {
                ComprehensiveMigrationUtility.ComprehensiveMigrationResult result = getValue();
                Platform.runLater(() -> handleMigrationResult(result));
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ Migration failed");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    logArea.appendText("❌ Migration failed: " + getException().getMessage() + "\n");
                    migrateButton.setDisable(false);
                    cancelButton.setDisable(false);
                    progressBar.setVisible(false);
                });
            }
        };
        
        new Thread(task).start();
    }
    
    /**
     * Handle migration result
     */
    private void handleMigrationResult(ComprehensiveMigrationUtility.ComprehensiveMigrationResult result) {
        progressBar.setVisible(false);
        
        if (result.isSuccess()) {
            statusLabel.setText("✅ Migration completed successfully");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            
            migrateButton.setText("✅ Migration Complete");
            cancelButton.setText("Close");
            cancelButton.setDisable(false);
            
            logArea.appendText("✅ Migration completed successfully!\n\n");
            
        } else {
            statusLabel.setText("❌ Migration failed");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            
            migrateButton.setDisable(false);
            cancelButton.setDisable(false);
            
            logArea.appendText("❌ Migration failed: " + result.getMessage() + "\n\n");
        }
        
        // Display migration log
        logArea.appendText("📋 Migration Log:\n");
        for (String logEntry : result.getMigrationLog()) {
            logArea.appendText(logEntry + "\n");
        }
        
        if (!result.getBackupPaths().isEmpty()) {
            logArea.appendText("\n📦 Backups created:\n");
            for (String backupPath : result.getBackupPaths()) {
                logArea.appendText("• " + backupPath + "\n");
            }
        }
        
        // Scroll to bottom
        logArea.setScrollTop(Double.MAX_VALUE);
    }
    
    /**
     * Get icon for security level
     */
    private String getSecurityLevelIcon(SecurityValidationSystem.SecurityLevel level) {
        switch (level) {
            case SECURE: return "✅";
            case INFO: return "ℹ️";
            case WARNING: return "⚠️";
            case HIGH: return "🔶";
            case CRITICAL: return "🔴";
            default: return "❓";
        }
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }
}