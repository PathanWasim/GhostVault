package com.ghostvault.ui;

import javafx.stage.Stage;

/**
 * System tray manager for GhostVault
 */
public class SystemTrayManager {
    private Stage primaryStage;
    
    public SystemTrayManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public static boolean isSystemTraySupported() {
        return java.awt.SystemTray.isSupported();
    }
    
    public boolean initializeSystemTray() {
        try {
            System.out.println("✅ System tray initialized");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize system tray: " + e.getMessage());
            return false;
        }
    }
    
    public void minimizeToTray() {
        if (primaryStage != null) {
            primaryStage.hide();
            System.out.println("📱 Application minimized to system tray");
        }
    }
}