package com.ghostvault.ui;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Manages system tray integration for GhostVault
 */
public class SystemTrayManager {
    
    private SystemTray systemTray;
    private TrayIcon trayIcon;
    private Stage primaryStage;
    private boolean isMinimizedToTray = false;
    
    public SystemTrayManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Initialize system tray if supported
     */
    public boolean initializeSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("⚠️ System tray is not supported on this platform");
            return false;
        }
        
        try {
            systemTray = SystemTray.getSystemTray();
            
            // Create tray icon
            Image trayImage = createTrayIcon();
            
            // Create popup menu
            PopupMenu popup = createPopupMenu();
            
            // Create tray icon
            trayIcon = new TrayIcon(trayImage, "GhostVault - Secure File Vault", popup);
            trayIcon.setImageAutoSize(true);
            
            // Add double-click listener to restore window
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(() -> restoreFromTray());
                }
            });
            
            // Add tray icon
            systemTray.add(trayIcon);
            
            System.out.println("✅ System tray initialized successfully");
            return true;
            
        } catch (AWTException e) {
            System.err.println("❌ Failed to initialize system tray: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create tray icon image
     */
    private Image createTrayIcon() {
        // Create a simple icon (16x16 pixels)
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a simple vault icon
        g2d.setColor(new Color(76, 175, 80)); // Green color
        g2d.fillRoundRect(2, 2, 12, 12, 4, 4);
        
        g2d.setColor(Color.WHITE);
        g2d.fillOval(6, 6, 4, 4); // Lock circle
        g2d.setColor(new Color(76, 175, 80));
        g2d.fillRect(7, 8, 2, 2); // Lock hole
        
        g2d.dispose();
        return image;
    }
    
    /**
     * Create popup menu for tray icon
     */
    private PopupMenu createPopupMenu() {
        PopupMenu popup = new PopupMenu();
        
        // Restore window
        MenuItem restoreItem = new MenuItem("Open GhostVault");
        restoreItem.addActionListener(e -> Platform.runLater(() -> restoreFromTray()));
        popup.add(restoreItem);
        
        popup.addSeparator();
        
        // Quick actions
        MenuItem lockItem = new MenuItem("Lock Vault");
        lockItem.addActionListener(e -> Platform.runLater(() -> {
            // Minimize to tray and clear sensitive data
            minimizeToTray();
        }));
        popup.add(lockItem);
        
        popup.addSeparator();
        
        // Exit
        MenuItem exitItem = new MenuItem("Exit GhostVault");
        exitItem.addActionListener(e -> Platform.runLater(() -> {
            // Proper shutdown
            if (primaryStage != null) {
                primaryStage.fireEvent(new javafx.stage.WindowEvent(
                    primaryStage, javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST));
            } else {
                Platform.exit();
            }
        }));
        popup.add(exitItem);
        
        return popup;
    }
    
    /**
     * Minimize application to system tray
     */
    public void minimizeToTray() {
        if (trayIcon != null && primaryStage != null) {
            Platform.runLater(() -> {
                primaryStage.hide();
                isMinimizedToTray = true;
                
                // Show notification
                if (trayIcon != null) {
                    trayIcon.displayMessage("GhostVault", 
                        "Application minimized to system tray", 
                        TrayIcon.MessageType.INFO);
                }
            });
        }
    }
    
    /**
     * Restore application from system tray
     */
    public void restoreFromTray() {
        if (primaryStage != null && isMinimizedToTray) {
            primaryStage.show();
            primaryStage.toFront();
            primaryStage.requestFocus();
            isMinimizedToTray = false;
        }
    }
    
    /**
     * Show tray notification
     */
    public void showNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }
    
    /**
     * Show tray notification with custom type
     */
    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }
    
    /**
     * Check if minimized to tray
     */
    public boolean isMinimizedToTray() {
        return isMinimizedToTray;
    }
    
    /**
     * Remove tray icon and cleanup
     */
    public void cleanup() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
            System.out.println("🧹 System tray cleaned up");
        }
    }
    
    /**
     * Check if system tray is available
     */
    public static boolean isSystemTraySupported() {
        return SystemTray.isSupported();
    }
}