package com.ghostvault.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Professional header component with branding, session info, and user controls
 */
public class ProfessionalHeader extends HBox {
    
    // Header sections
    private VBox brandingSection;
    private HBox sessionInfoSection;
    private HBox userControlsSection;
    private HBox statusSection;
    
    // Components
    private Label appTitleLabel;
    private Label appSubtitleLabel;
    private Label modeIndicator;
    private Label sessionTimeLabel;
    private Label userNameLabel;
    private Label connectionStatusLabel;
    private Button settingsButton;
    private Button logoutButton;
    private Button minimizeButton;
    private ProgressBar securityLevelBar;
    
    // Session data
    private String currentMode = "Master";
    private String userName = "User";
    private LocalDateTime sessionStart;
    private Timeline clockTimer;
    private boolean isConnected = true;
    
    // Callbacks
    private Consumer<String> onModeChange;
    private Runnable onSettingsClick;
    private Runnable onLogoutClick;
    private Runnable onMinimizeClick;
    
    public ProfessionalHeader() {
        this.sessionStart = LocalDateTime.now();
        initializeComponents();
        setupLayout();
        setupStyling();
        startClock();
    }
    
    /**
     * Initialize all header components
     */
    private void initializeComponents() {
        // Branding section
        appTitleLabel = new Label("👻 GhostVault");
        appTitleLabel.getStyleClass().add("app-title");
        
        appSubtitleLabel = new Label("Secure File Management");
        appSubtitleLabel.getStyleClass().add("app-subtitle");
        
        brandingSection = new VBox(2);
        brandingSection.getChildren().addAll(appTitleLabel, appSubtitleLabel);
        brandingSection.setAlignment(Pos.CENTER_LEFT);
        
        // Session info section
        modeIndicator = new Label();
        updateModeIndicator();
        
        sessionTimeLabel = new Label();
        sessionTimeLabel.getStyleClass().add("session-time");
        
        userNameLabel = new Label("👤 " + userName);
        userNameLabel.getStyleClass().add("user-name");
        
        connectionStatusLabel = new Label();
        updateConnectionStatus();
        
        sessionInfoSection = new HBox(15);
        sessionInfoSection.setAlignment(Pos.CENTER);
        sessionInfoSection.getChildren().addAll(
            modeIndicator,
            createSeparator(),
            sessionTimeLabel,
            createSeparator(),
            userNameLabel,
            createSeparator(),
            connectionStatusLabel
        );
        
        // Security level indicator
        securityLevelBar = new ProgressBar(0.85);
        securityLevelBar.getStyleClass().add("security-level-bar");
        securityLevelBar.setPrefWidth(100);
        securityLevelBar.setPrefHeight(6);
        
        Label securityLabel = new Label("Security Level");
        securityLabel.getStyleClass().add("security-label");
        
        VBox securitySection = new VBox(2);
        securitySection.setAlignment(Pos.CENTER);
        securitySection.getChildren().addAll(securityLabel, securityLevelBar);
        
        // User controls section
        settingsButton = new Button();
        settingsButton.setText(ModernIcons.SETTINGS);
        settingsButton.getStyleClass().addAll("header-button", "icon-button");
        settingsButton.setTooltip(new Tooltip("Settings"));
        settingsButton.setOnAction(e -> {
            if (onSettingsClick != null) onSettingsClick.run();
        });
        
        logoutButton = new Button();
        logoutButton.setText(ModernIcons.LOGOUT);
        logoutButton.getStyleClass().addAll("header-button", "icon-button", "logout-button");
        logoutButton.setTooltip(new Tooltip("Logout"));
        logoutButton.setOnAction(e -> {
            if (onLogoutClick != null) onLogoutClick.run();
        });
        
        minimizeButton = new Button();
        minimizeButton.setText("➖");
        minimizeButton.getStyleClass().addAll("header-button", "icon-button");
        minimizeButton.setTooltip(new Tooltip("Minimize"));
        minimizeButton.setOnAction(e -> {
            if (onMinimizeClick != null) onMinimizeClick.run();
        });
        
        userControlsSection = new HBox(5);
        userControlsSection.setAlignment(Pos.CENTER_RIGHT);
        userControlsSection.getChildren().addAll(
            securitySection,
            createSeparator(),
            settingsButton,
            logoutButton,
            minimizeButton
        );
        
        // Status section for notifications
        statusSection = new HBox(10);
        statusSection.setAlignment(Pos.CENTER);
        statusSection.getStyleClass().add("status-section");
    }
    
    /**
     * Setup the main layout
     */
    private void setupLayout() {
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(12, 20, 12, 20));
        this.setSpacing(20);
        
        // Create spacers for proper alignment
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.SOMETIMES);
        
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.SOMETIMES);
        
        // Add all sections to header
        this.getChildren().addAll(
            brandingSection,
            leftSpacer,
            sessionInfoSection,
            statusSection,
            rightSpacer,
            userControlsSection
        );
    }
    
    /**
     * Setup component styling
     */
    private void setupStyling() {
        this.getStyleClass().add("professional-header");
        
        // Apply modern styling
        this.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2d2d2d, #1a1a1a);" +
            "-fx-border-color: #404040;" +
            "-fx-border-width: 0 0 1px 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 2);"
        );
    }
    
    /**
     * Create a visual separator
     */
    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setPrefHeight(20);
        separator.setStyle("-fx-background-color: #555555;");
        return separator;
    }
    
    /**
     * Start the clock timer
     */
    private void startClock() {
        clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateSessionTime()));
        clockTimer.setCycleCount(Timeline.INDEFINITE);
        clockTimer.play();
        updateSessionTime(); // Initial update
    }
    
    /**
     * Update session time display
     */
    private void updateSessionTime() {
        Platform.runLater(() -> {
            LocalDateTime now = LocalDateTime.now();
            java.time.Duration sessionDuration = java.time.Duration.between(sessionStart, now);
            
            long hours = sessionDuration.toHours();
            long minutes = sessionDuration.toMinutes() % 60;
            long seconds = sessionDuration.getSeconds() % 60;
            
            String timeText = String.format("🕐 Session: %02d:%02d:%02d", hours, minutes, seconds);
            sessionTimeLabel.setText(timeText);
        });
    }
    
    /**
     * Update mode indicator
     */
    private void updateModeIndicator() {
        String modeIcon;
        String modeColor;
        
        switch (currentMode.toLowerCase()) {
            case "master":
                modeIcon = ModernIcons.MASTER_MODE;
                modeColor = "#107c10"; // Green
                break;
            case "decoy":
                modeIcon = ModernIcons.DECOY_MODE;
                modeColor = "#ff8c00"; // Orange
                break;
            case "panic":
                modeIcon = ModernIcons.PANIC_MODE;
                modeColor = "#d13438"; // Red
                break;
            default:
                modeIcon = ModernIcons.SECURE;
                modeColor = "#0078d4"; // Blue
        }
        
        modeIndicator.setText(modeIcon + " " + currentMode + " Mode");
        modeIndicator.setStyle(String.format(
            "-fx-text-fill: %s; -fx-font-weight: bold; -fx-font-size: 13px;", 
            modeColor
        ));
        modeIndicator.getStyleClass().add("mode-indicator");
    }
    
    /**
     * Update connection status
     */
    private void updateConnectionStatus() {
        if (isConnected) {
            connectionStatusLabel.setText(ModernIcons.CONNECTED + " Online");
            connectionStatusLabel.setStyle("-fx-text-fill: #107c10; -fx-font-size: 12px;");
        } else {
            connectionStatusLabel.setText(ModernIcons.DISCONNECTED + " Offline");
            connectionStatusLabel.setStyle("-fx-text-fill: #d13438; -fx-font-size: 12px;");
        }
        connectionStatusLabel.getStyleClass().add("connection-status");
    }
    
    /**
     * Show a temporary status message
     */
    public void showStatusMessage(String message, String type, Duration duration) {
        Platform.runLater(() -> {
            Label statusLabel = new Label(message);
            statusLabel.getStyleClass().add("status-message");
            
            // Set color based on type
            String color;
            switch (type.toLowerCase()) {
                case "success":
                    color = "#107c10";
                    statusLabel.setText(ModernIcons.SUCCESS + " " + message);
                    break;
                case "error":
                    color = "#d13438";
                    statusLabel.setText(ModernIcons.ERROR + " " + message);
                    break;
                case "warning":
                    color = "#ff8c00";
                    statusLabel.setText(ModernIcons.WARNING + " " + message);
                    break;
                default:
                    color = "#0078d4";
                    statusLabel.setText(ModernIcons.INFO + " " + message);
            }
            
            statusLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 12px;", color));
            
            // Add to status section
            statusSection.getChildren().clear();
            statusSection.getChildren().add(statusLabel);
            
            // Fade in
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), statusLabel);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
            // Auto-remove after duration
            Timeline removeTimer = new Timeline(new KeyFrame(duration, e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(300), statusLabel);
                fadeOut.setFromValue(1);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(event -> statusSection.getChildren().remove(statusLabel));
                fadeOut.play();
            }));
            removeTimer.play();
        });
    }
    
    /**
     * Update security level (0.0 to 1.0)
     */
    public void updateSecurityLevel(double level) {
        Platform.runLater(() -> {
            securityLevelBar.setProgress(level);
            
            // Change color based on level
            String color;
            if (level >= 0.8) {
                color = "#107c10"; // Green - High security
            } else if (level >= 0.5) {
                color = "#ff8c00"; // Orange - Medium security
            } else {
                color = "#d13438"; // Red - Low security
            }
            
            securityLevelBar.setStyle(String.format(
                "-fx-accent: %s; -fx-background-color: rgba(255,255,255,0.1);", 
                color
            ));
        });
    }
    
    /**
     * Add notification badge to settings button
     */
    public void showNotificationBadge(int count) {
        Platform.runLater(() -> {
            if (count > 0) {
                settingsButton.setText(ModernIcons.SETTINGS + " (" + count + ")");
                settingsButton.getStyleClass().add("has-notification");
            } else {
                settingsButton.setText(ModernIcons.SETTINGS);
                settingsButton.getStyleClass().remove("has-notification");
            }
        });
    }
    
    // Getters and Setters
    
    public String getCurrentMode() {
        return currentMode;
    }
    
    public void setCurrentMode(String mode) {
        this.currentMode = mode;
        updateModeIndicator();
        if (onModeChange != null) {
            onModeChange.accept(mode);
        }
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
        userNameLabel.setText("👤 " + userName);
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public void setConnected(boolean connected) {
        this.isConnected = connected;
        updateConnectionStatus();
    }
    
    public void setAppTitle(String title) {
        appTitleLabel.setText("👻 " + title);
    }
    
    public void setAppSubtitle(String subtitle) {
        appSubtitleLabel.setText(subtitle);
    }
    
    // Event handlers
    
    public void setOnModeChange(Consumer<String> callback) {
        this.onModeChange = callback;
    }
    
    public void setOnSettingsClick(Runnable callback) {
        this.onSettingsClick = callback;
    }
    
    public void setOnLogoutClick(Runnable callback) {
        this.onLogoutClick = callback;
    }
    
    public void setOnMinimizeClick(Runnable callback) {
        this.onMinimizeClick = callback;
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
    }
    
    /**
     * Reset session timer
     */
    public void resetSessionTimer() {
        this.sessionStart = LocalDateTime.now();
        updateSessionTime();
    }
    
    /**
     * Get session duration in seconds
     */
    public long getSessionDurationSeconds() {
        return java.time.Duration.between(sessionStart, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Show quick action buttons for emergency situations
     */
    public void showEmergencyControls(boolean show) {
        Platform.runLater(() -> {
            if (show) {
                Button panicButton = new Button(ModernIcons.PANIC_MODE);
                panicButton.getStyleClass().addAll("header-button", "panic-button");
                panicButton.setTooltip(new Tooltip("Emergency Mode"));
                panicButton.setOnAction(e -> setCurrentMode("Panic"));
                
                if (!userControlsSection.getChildren().contains(panicButton)) {
                    userControlsSection.getChildren().add(0, panicButton);
                }
            } else {
                userControlsSection.getChildren().removeIf(node -> 
                    node instanceof Button && ((Button) node).getStyleClass().contains("panic-button"));
            }
        });
    }
    
    /**
     * Animate mode change
     */
    public void animateModeChange(String newMode) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), modeIndicator);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            setCurrentMode(newMode);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), modeIndicator);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }
}