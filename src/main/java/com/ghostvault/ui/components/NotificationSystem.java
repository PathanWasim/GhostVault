package com.ghostvault.ui.components;

import javafx.stage.Stage;

/**
 * Simple NotificationSystem stub for compilation
 */
public class NotificationSystem {
    private static Stage primaryStage;
    
    public static void initialize(Stage stage) {
        primaryStage = stage;
    }
    
    public static void showInfo(String title, String message) {
        System.out.println("INFO: " + title + " - " + message);
    }
    
    public static void showWarning(String title, String message) {
        System.out.println("WARNING: " + title + " - " + message);
    }
    
    public static void showError(String title, String message) {
        System.err.println("ERROR: " + title + " - " + message);
    }
    
    public static NotificationSystem getInstance() {
        return new NotificationSystem();
    }
    
    public static class ProgressNotification {
        private String title;
        private String message;
        
        public ProgressNotification(String title, String message) {
            this.title = title;
            this.message = message;
        }
        
        public void complete(String message) {
            System.out.println("COMPLETED: " + title + " - " + message);
        }
    }
    
    public static ProgressNotification showProgress(String title, String message) {
        System.out.println("PROGRESS: " + title + " - " + message);
        return new ProgressNotification(title, message);
    }
    
    public static void showSuccess(String title, String message) {
        System.out.println("SUCCESS: " + title + " - " + message);
    }
}