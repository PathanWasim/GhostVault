package com.ghostvault.ui.components;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Professional header component with branding and session information
 */
public class ProfessionalHeader extends HBox {
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    // Header components
    private final Label brandingLabel;
    private final Label versionLabel;
    private final Label sessionStatusLabel;
    private final Label userInfoLabel;
    private final Label timeLabel;
    private final Label securityStatusLabel;
    
    // Session information
    private String currentUser = "User";
    private String sessionMode = "MASTER";
    private boolean isSecure = true;
    private LocalDateTime sessionStart;
    
    // Animation components
    private FadeTransition securityBlink;
    
    public ProfessionalHeader() {
        super(20);
        setPadding(new Insets(12, 20, 12, 20));
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("professional-header");
        
        sessionStart = LocalDateTime.now();
        
        // Left side - Branding
        VBox brandingBox = createBrandingSection();
        
        // Center spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Right side - Session info
        HBox sessionBox = createSessionInfoSection();
        
        getChildren().addAll(brandingBox, spacer, sessionBox);
        
        // Start time updates
        startTimeUpdates();
        
        // Apply professional styling
        setStyle("""
            -fx-background-color: linear-gradient(to bottom, #2d2d2d, #252525);
            -fx-border-color: #404040;
            -fx-border-width: 0 0 1px 0;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);
            """);
    }
    
    /**
     * Create branding section with logo and version
     */
    private VBox createBrandingSection() {
        VBox brandingBox = new VBox(2);
        brandingBox.setAlignment(Pos.CENTER_LEFT);
        
        // Main brand label
        brandingLabel = new Label("🔒 GhostVault");
        brandingLabel.setStyle("""
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: linear-gradient(to right, #0078d4, #00bcf2);
            """);
        
        // Version label
        versionLabel = new Label("Professional Edition v1.0.0");
        versionLabel.setStyle("""
            -fx-font-size: 11px;
            -fx-text-fill: #888888;
            -fx-font-style: italic;
            """);
        
        brandingBox.getChildren().addAll(brandingLabel, versionLabel);
        
        return brandingBox;
    }
    
    /**
     * Create session information section
     */
    private HBox createSessionInfoSection() {
        HBox sessionBox = new HBox(15);
        sessionBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Security status indicator
        securityStatusLabel = new Label("🛡️ SECURE");
        securityStatusLabel.getStyleClass().add("status-badge");
        securityStatusLabel.getStyleClass().add("status-online");
        securityStatusLabel.setTooltip(new Tooltip("Vault is encrypted and secure"));
        
        // Session mode indicator
        sessionStatusLabel = new Label("🔐 MASTER");
        sessionStatusLabel.getStyleClass().add("status-badge");
        sessionStatusLabel.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white;");
        sessionStatusLabel.setTooltip(new Tooltip("Current session mode"));
        
        // User information
        userInfoLabel = new Label("👤 " + currentUser);
        userInfoLabel.setStyle("""
            -fx-text-fill: #cccccc;
            -fx-font-size: 13px;
            -fx-font-weight: 500;
            """);
        userInfoLabel.setTooltip(new Tooltip("Current user"));
        
        // Time display
        timeLabel = new Label();
        timeLabel.setStyle("""
            -fx-text-fill: #cccccc;
            -fx-font-size: 13px;
            -fx-font-family: 'Consolas', monospace;
            """);
        updateTimeDisplay();
        
        sessionBox.getChildren().addAll(securityStatusLabel, sessionStatusLabel, userInfoLabel, timeLabel);
        
        return sessionBox;
    }
    
    /**
     * Update session mode (MASTER, DECOY, PANIC)
     */
    public void setSessionMode(String mode) {
        this.sessionMode = mode.toUpperCase();
        
        Platform.runLater(() -> {
            switch (sessionMode) {
                case "MASTER":
                    sessionStatusLabel.setText("🔐 MASTER");
                    sessionStatusLabel.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white;");
                    sessionStatusLabel.setTooltip(new Tooltip("Master vault - Full access"));
                    break;
                    
                case "DECOY":
                    sessionStatusLabel.setText("🎭 DECOY");
                    sessionStatusLabel.setStyle("-fx-background-color: #ff8c00; -fx-text-fill: white;");
                    sessionStatusLabel.setTooltip(new Tooltip("Decoy mode - Showing fake data"));
                    break;
                    
                case "PANIC":
                    sessionStatusLabel.setText("🚨 PANIC");
                    sessionStatusLabel.setStyle("-fx-background-color: #d13438; -fx-text-fill: white;");
                    sessionStatusLabel.setTooltip(new Tooltip("Panic mode - Data destruction"));
                    break;
                    
                default:
                    sessionStatusLabel.setText("❓ UNKNOWN");
                    sessionStatusLabel.setStyle("-fx-background-color: #888888; -fx-text-fill: white;");
                    sessionStatusLabel.setTooltip(new Tooltip("Unknown session mode"));
            }
        });
    }
    
    /**
     * Update security status
     */
    public void setSecurityStatus(boolean secure) {
        this.isSecure = secure;
        
        Platform.runLater(() -> {
            if (secure) {
                securityStatusLabel.setText("🛡️ SECURE");
                securityStatusLabel.getStyleClass().removeAll("status-warning", "status-error");
                securityStatusLabel.getStyleClass().add("status-online");
                securityStatusLabel.setTooltip(new Tooltip("Vault is encrypted and secure"));
                stopSecurityBlink();
            } else {
                securityStatusLabel.setText("⚠️ UNSECURE");
                securityStatusLabel.getStyleClass().removeAll("status-online");
                securityStatusLabel.getStyleClass().add("status-warning");
                securityStatusLabel.setTooltip(new Tooltip("Security warning detected"));
                startSecurityBlink();
            }
        });
    }
    
    /**
     * Update user information
     */
    public void setUserInfo(String username) {
        this.currentUser = username != null ? username : "User";
        
        Platform.runLater(() -> {
            userInfoLabel.setText("👤 " + currentUser);
            userInfoLabel.setTooltip(new Tooltip("Current user: " + currentUser));
        });
    }
    
    /**
     * Show session duration
     */
    public void showSessionDuration(long durationMinutes) {
        Platform.runLater(() -> {
            String durationText = formatDuration(durationMinutes);
            userInfoLabel.setTooltip(new Tooltip(String.format("Session duration: %s", durationText)));
        });
    }
    
    /**
     * Show threat level indicator
     */
    public void setThreatLevel(String level) {
        Platform.runLater(() -> {
            switch (level.toUpperCase()) {
                case "LOW":
                    securityStatusLabel.setText("🛡️ SECURE");
                    securityStatusLabel.getStyleClass().removeAll("status-warning", "status-error");
                    securityStatusLabel.getStyleClass().add("status-online");
                    stopSecurityBlink();
                    break;
                    
                case "MEDIUM":
                    securityStatusLabel.setText("⚠️ CAUTION");
                    securityStatusLabel.getStyleClass().removeAll("status-online", "status-error");
                    securityStatusLabel.getStyleClass().add("status-warning");
                    startSecurityBlink();
                    break;
                    
                case "HIGH":
                    securityStatusLabel.setText("🚨 THREAT");
                    securityStatusLabel.getStyleClass().removeAll("status-online", "status-warning");
                    securityStatusLabel.getStyleClass().add("status-error");
                    startSecurityBlink();
                    break;
            }
        });
    }
    
    /**
     * Update time display
     */
    private void updateTimeDisplay() {
        LocalDateTime now = LocalDateTime.now();
        String timeText = now.format(TIME_FORMATTER);
        String dateText = now.format(DATE_FORMATTER);
        
        timeLabel.setText(timeText);
        timeLabel.setTooltip(new Tooltip(dateText));
    }
    
    /**
     * Start automatic time updates
     */
    private void startTimeUpdates() {
        Thread timeThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Update every second
                    Platform.runLater(this::updateTimeDisplay);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();
    }
    
    /**
     * Start security status blinking animation
     */
    private void startSecurityBlink() {
        if (securityBlink != null) {
            securityBlink.stop();
        }
        
        securityBlink = new FadeTransition(Duration.seconds(0.8), securityStatusLabel);
        securityBlink.setFromValue(1.0);
        securityBlink.setToValue(0.3);
        securityBlink.setCycleCount(FadeTransition.INDEFINITE);
        securityBlink.setAutoReverse(true);
        securityBlink.play();
    }
    
    /**
     * Stop security status blinking animation
     */
    private void stopSecurityBlink() {
        if (securityBlink != null) {
            securityBlink.stop();
            securityStatusLabel.setOpacity(1.0);
        }
    }
    
    /**
     * Format duration for display
     */
    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        } else if (minutes < 1440) { // Less than 24 hours
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return String.format("%d hours, %d minutes", hours, remainingMinutes);
        } else {
            long days = minutes / 1440;
            long remainingHours = (minutes % 1440) / 60;
            return String.format("%d days, %d hours", days, remainingHours);
        }
    }
    
    /**
     * Show connection status
     */
    public void setConnectionStatus(boolean connected) {
        Platform.runLater(() -> {
            if (connected) {
                versionLabel.setText("Professional Edition v1.0.0 • Online");
                versionLabel.setStyle("""
                    -fx-font-size: 11px;
                    -fx-text-fill: #107c10;
                    -fx-font-style: italic;
                    """);
            } else {
                versionLabel.setText("Professional Edition v1.0.0 • Offline");
                versionLabel.setStyle("""
                    -fx-font-size: 11px;
                    -fx-text-fill: #888888;
                    -fx-font-style: italic;
                    """);
            }
        });
    }
    
    /**
     * Show notification badge
     */
    public void showNotificationBadge(int count) {
        Platform.runLater(() -> {
            if (count > 0) {
                brandingLabel.setText(String.format("🔒 GhostVault (%d)", count));
            } else {
                brandingLabel.setText("🔒 GhostVault");
            }
        });
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        stopSecurityBlink();
    }
}