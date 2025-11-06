package com.ghostvault.ui;

/**
 * Simple NotificationManager stub for compilation
 */
public class NotificationManager {
    
    public void showNotification(String title, String message) {
        System.out.println("Notification: " + title + " - " + message);
    }
    
    public void showError(String title, String message) {
        System.err.println("Error: " + title + " - " + message);
    }
    
    public void showSuccess(String title, String message) {
        System.out.println("Success: " + title + " - " + message);
    }
    
    public void showWarning(String title, String message) {
        System.out.println("Warning: " + title + " - " + message);
    }
}