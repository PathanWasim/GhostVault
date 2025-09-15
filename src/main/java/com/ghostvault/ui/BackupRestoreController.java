package com.ghostvault.ui;

import com.ghostvault.core.*;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * UI Controller for backup and restore operations
 */
public class BackupRestoreController {
    
    private Stage dialogStage;
    private BackupManager backupManager;
    private SecretKey encryptionKey;
    
    // UI Components
    private TabPane tabPane;
    private ProgressBar progressBar;
    private Label statusLabel;
    private TextArea logArea;
    private Button backupButton;
    private Button restoreButton;
    private Button verifyButton;
    
    // Backup options
    private CheckBox includeConfigCheckBox;
    private CheckBox continueOnErrorCheckBox;
    private TextField backupLocationField;
    
    // Restore options
    private CheckBox overwriteExistingCheckBox;
    private CheckBox restoreConfigCheckBox;
    private CheckBox backupBeforeRestoreCheckBox;
    private TextField restoreFileField;
    
    public BackupRestoreController(BackupManager backupManager, SecretKey encryptionKey) {
        this.backupManager = backupManager;
        this.encryptionKey = encryptionKey;
        this.backupManager.setBackupEncryptionKey(encryptionKey);
    }
    
    /**
     * Show backup/restore dialog
     */
    public void showDialog(Stage parentStage) {
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parentStage);
        dialogStage.setTitle("GhostVault - Backup & Restore");
        dialogStage.setResizable(true);
        
        VBox root = createUI();
        Scene scene = new Scene(root, 700, 600);
        dialogStage.setScene(scene);
        
        dialogStage.show();
    }
    
    /**
     * Create the main UI
     */
    private VBox createUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("Backup & Restore");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        // Tab pane for backup/restore/verify
        tabPane = new TabPane();
        tabPane.getTabs().addAll(
            createBackupTab(),
            createRestoreTab(),
            createVerifyTab()
        );
        
        // Progress section
        VBox progressSection = createProgressSection();
        
        // Log area
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(8);
        logArea.setWrapText(true);
        
        // Buttons
        HBox buttonBox = createButtonBox();
        
        root.getChildren().addAll(
            titleLabel,
            new Separator(),
            tabPane,
            progressSection,
            new Label("Operation Log:"),
            logArea,
            buttonBox
        );
        
        return root;
    }
    
    /**
     * Create backup tab
     */
    private Tab createBackupTab() {
        Tab backupTab = new Tab("Backup");
        backupTab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        // Backup location
        Label locationLabel = new Label("Backup Location:");
        locationLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox locationBox = new HBox(10);
        backupLocationField = new TextField();
        backupLocationField.setPrefWidth(400);
        backupLocationField.setText(getDefaultBackupLocation());
        
        Button browseButton = new Button("Browse...");
        browseButton.setOnAction(e -> browseForBackupLocation());
        
        locationBox.getChildren().addAll(backupLocationField, browseButton);
        
        // Backup options
        Label optionsLabel = new Label("Backup Options:");
        optionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        includeConfigCheckBox = new CheckBox("Include configuration files");
        includeConfigCheckBox.setSelected(true);
        
        continueOnErrorCheckBox = new CheckBox("Continue on errors");
        continueOnErrorCheckBox.setSelected(false);
        
        // Backup info
        Label infoLabel = new Label("Backup Information:");
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label infoText = new Label(
            "• Creates an encrypted backup of all vault files and metadata\\n" +
            "• Backup files use .gvb extension (GhostVault Backup)\\n" +
            "• Configuration includes passwords and vault settings\\n" +
            "• Backups maintain full encryption and security"
        );
        infoText.setWrapText(true);
        
        content.getChildren().addAll(
            locationLabel, locationBox,
            new Separator(),
            optionsLabel, includeConfigCheckBox, continueOnErrorCheckBox,
            new Separator(),
            infoLabel, infoText
        );
        
        backupTab.setContent(content);
        return backupTab;
    }
    
    /**
     * Create restore tab
     */
    private Tab createRestoreTab() {
        Tab restoreTab = new Tab("Restore");
        restoreTab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        // Restore file
        Label fileLabel = new Label("Backup File:");
        fileLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox fileBox = new HBox(10);
        restoreFileField = new TextField();
        restoreFileField.setPrefWidth(400);
        
        Button browseRestoreButton = new Button("Browse...");
        browseRestoreButton.setOnAction(e -> browseForRestoreFile());
        
        fileBox.getChildren().addAll(restoreFileField, browseRestoreButton);
        
        // Restore options
        Label optionsLabel = new Label("Restore Options:");
        optionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        overwriteExistingCheckBox = new CheckBox("Overwrite existing files");
        overwriteExistingCheckBox.setSelected(false);
        
        restoreConfigCheckBox = new CheckBox("Restore configuration");
        restoreConfigCheckBox.setSelected(true);
        
        backupBeforeRestoreCheckBox = new CheckBox("Backup existing vault before restore");
        backupBeforeRestoreCheckBox.setSelected(true);
        
        // Warning
        Label warningLabel = new Label("⚠️ Warning:");
        warningLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        warningLabel.setTextFill(Color.ORANGE);
        
        Label warningText = new Label(
            "Restoring will replace current vault contents. " +
            "It's recommended to create a backup of your current vault first."
        );
        warningText.setWrapText(true);
        warningText.setTextFill(Color.DARKORANGE);
        
        content.getChildren().addAll(
            fileLabel, fileBox,
            new Separator(),
            optionsLabel, overwriteExistingCheckBox, restoreConfigCheckBox, backupBeforeRestoreCheckBox,
            new Separator(),
            warningLabel, warningText
        );
        
        restoreTab.setContent(content);
        return restoreTab;
    }
    
    /**
     * Create verify tab
     */
    private Tab createVerifyTab() {
        Tab verifyTab = new Tab("Verify");
        verifyTab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        
        Label infoLabel = new Label("Backup Verification");
        infoLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label descLabel = new Label(
            "Verify the integrity of a backup file without restoring it. " +
            "This checks that the backup is valid and can be restored successfully."
        );
        descLabel.setWrapText(true);
        
        // File selection (reuse restore file field)
        Label fileLabel = new Label("Select backup file to verify:");
        
        content.getChildren().addAll(
            infoLabel, descLabel,
            new Separator(),
            fileLabel
        );
        
        verifyTab.setContent(content);
        return verifyTab;
    }
    
    /**
     * Create progress section
     */
    private VBox createProgressSection() {
        VBox section = new VBox(5);
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);
        
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("System", 11));
        
        section.getChildren().addAll(progressBar, statusLabel);
        return section;
    }
    
    /**
     * Create button box
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        backupButton = new Button("Create Backup");
        backupButton.setPrefWidth(120);
        backupButton.setOnAction(e -> performBackup());
        
        restoreButton = new Button("Restore");
        restoreButton.setPrefWidth(120);
        restoreButton.setOnAction(e -> performRestore());
        
        verifyButton = new Button("Verify Backup");
        verifyButton.setPrefWidth(120);
        verifyButton.setOnAction(e -> performVerify());
        
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(120);
        closeButton.setOnAction(e -> dialogStage.close());
        
        // Show appropriate buttons based on selected tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            buttonBox.getChildren().clear();
            
            if (newTab.getText().equals("Backup")) {
                buttonBox.getChildren().addAll(backupButton, closeButton);
            } else if (newTab.getText().equals("Restore")) {
                buttonBox.getChildren().addAll(restoreButton, closeButton);
            } else if (newTab.getText().equals("Verify")) {
                buttonBox.getChildren().addAll(verifyButton, closeButton);
            }
        });
        
        // Initialize with backup tab
        buttonBox.getChildren().addAll(backupButton, closeButton);
        
        return buttonBox;
    }
    
    /**
     * Browse for backup location
     */
    private void browseForBackupLocation() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup As");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup Files", "*.gvb")
        );
        
        File file = fileChooser.showSaveDialog(dialogStage);
        if (file != null) {
            backupLocationField.setText(file.getAbsolutePath());
        }
    }
    
    /**
     * Browse for restore file
     */
    private void browseForRestoreFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Backup File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("GhostVault Backup Files", "*.gvb")
        );
        
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            restoreFileField.setText(file.getAbsolutePath());
        }
    }
    
    /**
     * Get default backup location
     */
    private String getDefaultBackupLocation() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return System.getProperty("user.home") + "/GhostVault_Backup_" + timestamp + ".gvb";
    }
    
    /**
     * Perform backup operation
     */
    private void performBackup() {
        String backupPath = backupLocationField.getText().trim();
        if (backupPath.isEmpty()) {
            showError("Please specify a backup location");
            return;
        }
        
        File backupFile = new File(backupPath);
        
        // Create backup options
        BackupOptions options = new BackupOptions();
        options.includeConfiguration = includeConfigCheckBox.isSelected();
        options.continueOnError = continueOnErrorCheckBox.isSelected();
        
        // Disable UI
        setUIEnabled(false);
        showProgress("Creating backup...");
        
        Task<BackupResult> backupTask = new Task<BackupResult>() {
            @Override
            protected BackupResult call() throws Exception {
                return backupManager.createBackup(backupFile, options);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    BackupResult result = getValue();
                    hideProgress();
                    setUIEnabled(true);
                    
                    if (result.isSuccess()) {
                        logMessage("✅ Backup completed successfully");
                        logMessage("   Files backed up: " + result.getStats().getFilesBackedUp());
                        logMessage("   Backup size: " + com.ghostvault.util.FileUtils.formatFileSize(result.getStats().getFinalBackupSize()));
                        logMessage("   Duration: " + result.getStats().getBackupDuration() + "ms");
                        
                        showSuccess("Backup created successfully!\\nLocation: " + backupFile.getAbsolutePath());
                    } else {
                        logMessage("❌ Backup failed: " + result.getErrorMessage());
                        showError("Backup failed: " + result.getErrorMessage());
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    setUIEnabled(true);
                    
                    Throwable exception = getException();
                    String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                    logMessage("❌ Backup failed: " + errorMsg);
                    showError("Backup failed: " + errorMsg);
                });
            }
        };
        
        Thread backupThread = new Thread(backupTask);
        backupThread.setDaemon(true);
        backupThread.start();
    }
    
    /**
     * Perform restore operation
     */
    private void performRestore() {
        String restorePath = restoreFileField.getText().trim();
        if (restorePath.isEmpty()) {
            showError("Please select a backup file to restore");
            return;
        }
        
        File restoreFile = new File(restorePath);
        if (!restoreFile.exists()) {
            showError("Backup file not found: " + restorePath);
            return;
        }
        
        // Confirm restore operation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Restore");
        confirmAlert.setHeaderText("Restore from backup?");
        confirmAlert.setContentText("This will replace your current vault contents. Are you sure?");
        
        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        // Create restore options
        RestoreOptions options = new RestoreOptions();
        options.overwriteExisting = overwriteExistingCheckBox.isSelected();
        options.restoreConfiguration = restoreConfigCheckBox.isSelected();
        options.backupExistingVault = backupBeforeRestoreCheckBox.isSelected();
        options.continueOnError = true;
        
        // Disable UI
        setUIEnabled(false);
        showProgress("Restoring from backup...");
        
        Task<RestoreResult> restoreTask = new Task<RestoreResult>() {
            @Override
            protected RestoreResult call() throws Exception {
                return backupManager.restoreFromBackup(restoreFile, options);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    RestoreResult result = getValue();
                    hideProgress();
                    setUIEnabled(true);
                    
                    if (result.isSuccess()) {
                        logMessage("✅ Restore completed successfully");
                        logMessage("   Files restored: " + result.getStats().getFilesRestored());
                        logMessage("   Files skipped: " + result.getStats().getSkippedFiles());
                        logMessage("   Duration: " + result.getStats().getRestoreDuration() + "ms");
                        
                        showSuccess("Restore completed successfully!\\nFiles restored: " + result.getStats().getFilesRestored());
                    } else {
                        logMessage("❌ Restore failed: " + result.getErrorMessage());
                        showError("Restore failed: " + result.getErrorMessage());
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    setUIEnabled(true);
                    
                    Throwable exception = getException();
                    String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                    logMessage("❌ Restore failed: " + errorMsg);
                    showError("Restore failed: " + errorMsg);
                });
            }
        };
        
        Thread restoreThread = new Thread(restoreTask);
        restoreThread.setDaemon(true);
        restoreThread.start();
    }
    
    /**
     * Perform verify operation
     */
    private void performVerify() {
        String verifyPath = restoreFileField.getText().trim();
        if (verifyPath.isEmpty()) {
            showError("Please select a backup file to verify");
            return;
        }
        
        File verifyFile = new File(verifyPath);
        if (!verifyFile.exists()) {
            showError("Backup file not found: " + verifyPath);
            return;
        }
        
        // Disable UI
        setUIEnabled(false);
        showProgress("Verifying backup...");
        
        Task<BackupVerificationResult> verifyTask = new Task<BackupVerificationResult>() {
            @Override
            protected BackupVerificationResult call() throws Exception {
                return backupManager.verifyBackup(verifyFile);
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    BackupVerificationResult result = getValue();
                    hideProgress();
                    setUIEnabled(true);
                    
                    if (result.isValid()) {
                        logMessage("✅ Backup verification successful");
                        logMessage("   " + result.getBackupInfo());
                        
                        showSuccess("Backup is valid!\\n" + result.getBackupInfo());
                    } else {
                        logMessage("❌ Backup verification failed: " + result.getErrorMessage());
                        showError("Backup verification failed: " + result.getErrorMessage());
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    hideProgress();
                    setUIEnabled(true);
                    
                    Throwable exception = getException();
                    String errorMsg = exception != null ? exception.getMessage() : "Unknown error";
                    logMessage("❌ Backup verification failed: " + errorMsg);
                    showError("Backup verification failed: " + errorMsg);
                });
            }
        };
        
        Thread verifyThread = new Thread(verifyTask);
        verifyThread.setDaemon(true);
        verifyThread.start();
    }
    
    /**
     * Show progress indicator
     */
    private void showProgress(String message) {
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate
        statusLabel.setText(message);
        logMessage("🔄 " + message);
    }
    
    /**
     * Hide progress indicator
     */
    private void hideProgress() {
        progressBar.setVisible(false);
        statusLabel.setText("");
    }
    
    /**
     * Enable/disable UI controls
     */
    private void setUIEnabled(boolean enabled) {
        backupButton.setDisable(!enabled);
        restoreButton.setDisable(!enabled);
        verifyButton.setDisable(!enabled);
        tabPane.setDisable(!enabled);
    }
    
    /**
     * Log message to log area
     */
    private void logMessage(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.appendText("[" + timestamp + "] " + message + "\\n");
    }
    
    /**
     * Show success message
     */
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}