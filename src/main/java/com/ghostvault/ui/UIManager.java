package com.ghostvault.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Simple UI Manager for dialog operations
 */
public class UIManager {
    
    private Stage primaryStage;
    
    public UIManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Show information dialog
     */
    public void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }
    
    /**
     * Show warning dialog
     */
    public void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }
    
    /**
     * Show error dialog
     */
    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }
    
    /**
     * Show confirmation dialog
     */
    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(primaryStage);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Show login scene (stub for compatibility)
     */
    public void showLoginScene() {
        // This would normally switch to login scene
        // For now, just close the application
        javafx.application.Platform.exit();
    }
    
    /**
     * Set dark theme (stub for compatibility)
     */
    public void setDarkTheme(boolean darkTheme) {
        // Theme setting stub
    }
}