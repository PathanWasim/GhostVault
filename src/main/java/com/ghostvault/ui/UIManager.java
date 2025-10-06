package com.ghostvault.ui;

import com.ghostvault.backup.VaultBackupManager;
import com.ghostvault.core.DecoyManager;
import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive UI manager for all application scenes and transitions
 * Handles theme management, scene creation, and smooth transitions
 */
public class UIManager {
    
    private Stage primaryStage;
    private NotificationManager notificationManager;
    private AccessibilityManager accessibilityManager;
    private boolean isDarkTheme = true; // Default to dark theme
    private Map<String, Scene> sceneCache = new HashMap<>();
    private com.ghostvault.integration.ApplicationIntegrator applicationIntegrator;
    
    // Scene identifiers
    public static final String FIRST_RUN_SETUP_SCENE = "first_run_setup";
    public static final String LOGIN_SCENE = "login";
    public static final String MASTER_VAULT_SCENE = "master_vault";
    public static final String DECOY_VAULT_SCENE = "decoy_vault";
    public static final String BACKUP_RESTORE_SCENE = "backup_restore";
    
    /**
     * Initialize UI manager with primary stage
     */
    public void initialize(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize accessibility manager
        accessibilityManager = new AccessibilityManager();
        
        // Set up stage properties
        primaryStage.setTitle("GhostVault");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);
        
        // Set up window close handler
        primaryStage.setOnCloseRequest(event -> {
            // Handle graceful shutdown
            handleApplicationClose();
        });
        
        System.out.println("🎨 UI Manager initialized");
    }
    
    /**
     * Set notification manager
     */
    public void setNotificationManager(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }
    
    /**
     * Set application integrator
     */
    public void setApplicationIntegrator(com.ghostvault.integration.ApplicationIntegrator integrator) {
        this.applicationIntegrator = integrator;
    }
    
    /**
     * Create first run setup scene
     */
    public Scene createFirstRunSetupScene(com.ghostvault.security.PasswordManager passwordManager) throws IOException {
        // Don't cache this scene as it should only be shown once
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/initial_setup.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 900, 700);
        applyTheme(scene);
        
        // Get controller and set up
        InitialSetupController controller = loader.getController();
        if (controller != null) {
            controller.setUIManager(this);
            controller.setPasswordManager(passwordManager);
        }
        
        return scene;
    }
    
    /**
     * Create login scene
     */
    public Scene createLoginScene() throws IOException {
        // Don't cache login scene to ensure fresh state
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 800, 600);
        applyTheme(scene);
        
        // Get controller and set up
        LoginController controller = loader.getController();
        if (controller != null) {
            controller.setUIManager(this);
            controller.setApplicationIntegrator(applicationIntegrator);
        }
        
        return scene;
    }
    
    /**
     * Show login scene (navigate to login)
     */
    public void showLoginScene() {
        try {
            Scene loginScene = createLoginScene();
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("GhostVault - Login");
        } catch (Exception e) {
            System.err.println("Error showing login scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create master vault scene
     */
    public Scene createMasterVaultScene(FileManager fileManager, MetadataManager metadataManager, 
                                       VaultBackupManager backupManager, SecretKey encryptionKey) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vault_main.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1200, 800);
        applyTheme(scene);
        
        // Get controller and set up
        VaultMainController controller = loader.getController();
        if (controller != null) {
            controller.initialize(fileManager, metadataManager, backupManager, encryptionKey);
            controller.setUIManager(this);
        }
        
        return scene;
    }
    
    /**
     * Create decoy vault scene
     */
    public Scene createDecoyVaultScene(DecoyManager decoyManager) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/vault_main.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 1200, 800);
        applyTheme(scene);
        
        // Get controller and set up for decoy mode
        VaultMainController controller = loader.getController();
        if (controller != null) {
            controller.initializeDecoyMode(decoyManager);
            controller.setUIManager(this);
        }
        
        return scene;
    }
    
    /**
     * Create backup/restore scene
     */
    public Scene createBackupRestoreScene(VaultBackupManager backupManager, SecretKey encryptionKey) throws IOException {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/backup_restore.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 800, 700);
        applyTheme(scene);
        
        // Get controller and set up
        BackupRestoreController controller = loader.getController();
        if (controller != null) {
            controller.setBackupManager(backupManager, encryptionKey);
            controller.setStage(primaryStage);
        }
        
        return scene;
    }
    
    /**
     * Switch to scene with smooth transition
     */
    public void switchToScene(Scene newScene) {
        AnimationManager.smoothSceneTransition(primaryStage, newScene, () -> {
            // Apply theme and accessibility to new scene
            applyTheme(newScene);
        });
    }
    
    /**
     * Apply theme to scene
     */
    public void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        
        // Add professional base styling first
        scene.getStylesheets().add(getClass().getResource("/styles/professional.css").toExternalForm());
        
        if (isDarkTheme) {
            scene.getStylesheets().add(getClass().getResource("/styles/dark_theme.css").toExternalForm());
        } else {
            scene.getStylesheets().add(getClass().getResource("/styles/light_theme.css").toExternalForm());
        }
        
        // Add common styles (will override professional where needed)
        scene.getStylesheets().add(getClass().getResource("/styles/common.css").toExternalForm());
        
        // Initialize accessibility for the scene
        if (accessibilityManager != null) {
            accessibilityManager.initializeAccessibility(scene);
        }
    }
    
    /**
     * Toggle between dark and light themes
     */
    public void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        
        // Apply new theme to current scene
        if (primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
        
        // Clear scene cache to force recreation with new theme
        sceneCache.clear();
        
        // Notify user
        if (notificationManager != null) {
            String themeName = isDarkTheme ? "Dark" : "Light";
            notificationManager.showInfo("Theme Changed", "Switched to " + themeName + " theme");
        }
    }
    
    /**
     * Show progress dialog
     */
    public ProgressDialog showProgress(String title, String message) {
        ProgressDialog progressDialog = new ProgressDialog(primaryStage, title, message);
        progressDialog.show();
        return progressDialog;
    }
    
    /**
     * Show error dialog
     */
    public void showError(String title, String message) {
        if (notificationManager != null) {
            notificationManager.showError(title, message);
        } else {
            // Fallback to simple alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
    
    /**
     * Show warning dialog
     */
    public void showWarning(String title, String message) {
        if (notificationManager != null) {
            notificationManager.showWarning(title, message);
        } else {
            // Fallback to simple alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
    
    /**
     * Show info dialog
     */
    public void showInfo(String title, String message) {
        if (notificationManager != null) {
            notificationManager.showInfo(title, message);
        } else {
            // Fallback to simple alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
    
    /**
     * Show confirmation dialog
     */
    public boolean showConfirmation(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) == 
               javafx.scene.control.ButtonType.OK;
    }
    
    /**
     * Handle application close
     */
    private void handleApplicationClose() {
        // Show confirmation dialog
        boolean confirmed = showConfirmation("Exit GhostVault", 
            "Are you sure you want to exit GhostVault?");
        
        if (confirmed) {
            // Perform cleanup and shutdown
            System.out.println("🚪 Application closing...");
        }
    }
    
    /**
     * Get current theme status
     */
    public boolean isDarkTheme() {
        return isDarkTheme;
    }
    
    /**
     * Set theme
     */
    public void setDarkTheme(boolean darkTheme) {
        if (this.isDarkTheme != darkTheme) {
            toggleTheme();
        }
    }
    
    /**
     * Clear scene cache
     */
    public void clearSceneCache() {
        sceneCache.clear();
    }
    
    /**
     * Get primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Get accessibility manager
     */
    public AccessibilityManager getAccessibilityManager() {
        return accessibilityManager;
    }
    
    /**
     * Show success animation on node
     */
    public void showSuccessAnimation(javafx.scene.Node node) {
        AnimationManager.successGlow(node).play();
        AnimationManager.bounce(node).play();
    }
    
    /**
     * Show error animation on node
     */
    public void showErrorAnimation(javafx.scene.Node node) {
        AnimationManager.errorGlow(node).play();
        AnimationManager.shake(node).play();
    }
    
    /**
     * Show loading animation on node
     */
    public javafx.animation.RotateTransition showLoadingAnimation(javafx.scene.Node node) {
        javafx.animation.RotateTransition rotation = AnimationManager.continuousRotate(node);
        rotation.play();
        return rotation;
    }
    
    /**
     * Animate progress bar
     */
    public void animateProgress(javafx.scene.control.ProgressBar progressBar, double progress) {
        AnimationManager.animateProgress(progressBar, progressBar.getProgress(), progress).play();
    }
    
    /**
     * Show typewriter effect on label
     */
    public void showTypewriterEffect(javafx.scene.control.Label label, String text) {
        AnimationManager.typewriter(label, text).play();
    }
}