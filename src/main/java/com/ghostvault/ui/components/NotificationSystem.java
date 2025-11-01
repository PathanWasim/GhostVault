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
}