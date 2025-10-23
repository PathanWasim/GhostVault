package com.ghostvault.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

/**
 * Settings configuration dialog
 */
public class SettingsDialog extends Dialog<SettingsDialog.Settings> {
    
    private CheckBox darkThemeCheckBox;
    private CheckBox autoBackupCheckBox;
    private Slider sessionTimeoutSlider;
    private CheckBox notificationsCheckBox;
    private CheckBox secureDeleteCheckBox;
    
    public SettingsDialog() {
        initializeDialog();
        createContent();
        setupResultConverter();
    }
    
    private void initializeDialog() {
        setTitle("GhostVault Settings");
        setHeaderText("Configure your vault settings");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        
        // Add button types
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }
    
    private void createContent() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        
        // Appearance Section
        VBox appearanceSection = createSection("🎨 Appearance", createAppearanceControls());
        
        // Security Section
        VBox securitySection = createSection("🔒 Security", createSecurityControls());
        
        // Backup Section
        VBox backupSection = createSection("💾 Backup", createBackupControls());
        
        // Notifications Section
        VBox notificationSection = createSection("🔔 Notifications", createNotificationControls());
        
        mainContent.getChildren().addAll(
            appearanceSection,
            new Separator(),
            securitySection,
            new Separator(),
            backupSection,
            new Separator(),
            notificationSection
        );
        
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 600);
        
        getDialogPane().setContent(scrollPane);
    }
    
    private VBox createSection(String title, VBox content) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
        
        VBox section = new VBox(10);
        section.getChildren().addAll(titleLabel, content);
        return section;
    }
    
    private ComboBox<String> themeComboBox;
    
    private VBox createAppearanceControls() {
        VBox controls = new VBox(15);
        
        // Theme selection
        Label themeLabel = new Label("Interface Theme:");
        themeLabel.setStyle("-fx-font-weight: bold;");
        
        themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(
            "Modern Password Manager",
            "High-Tech Dark", 
            "Modern Light",
            "High Contrast",
            "Professional"
        );
        themeComboBox.setValue("Modern Password Manager"); // Default theme
        themeComboBox.setPrefWidth(200);
        
        Label themeInfo = new Label("Choose your preferred interface theme");
        themeInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        // Keep the old checkbox for backward compatibility
        darkThemeCheckBox = new CheckBox("Legacy Dark Theme Mode");
        darkThemeCheckBox.setSelected(false);
        darkThemeCheckBox.setVisible(false); // Hide it but keep for compatibility
        
        // Theme preview button
        Button previewButton = new Button("🎨 Preview Theme");
        previewButton.setOnAction(e -> previewSelectedTheme());
        
        controls.getChildren().addAll(
            themeLabel, 
            themeComboBox, 
            previewButton,
            themeInfo,
            darkThemeCheckBox
        );
        return controls;
    }
    
    private void previewSelectedTheme() {
        String selectedTheme = themeComboBox.getValue();
        if (selectedTheme != null) {
            com.ghostvault.ui.components.ModernThemeManager.Theme theme = getThemeFromDisplayName(selectedTheme);
            if (theme != null && getDialogPane().getScene() != null) {
                com.ghostvault.ui.components.ModernThemeManager.applyTheme(getDialogPane().getScene(), theme);
                
                // Show preview notification
                Alert preview = new Alert(Alert.AlertType.INFORMATION);
                preview.setTitle("Theme Preview");
                preview.setHeaderText("Theme Preview: " + selectedTheme);
                preview.setContentText("This is how the " + selectedTheme + " theme looks. Click OK to continue or Cancel to revert.");
                preview.showAndWait();
            }
        }
    }
    
    private com.ghostvault.ui.components.ModernThemeManager.Theme getThemeFromDisplayName(String displayName) {
        for (com.ghostvault.ui.components.ModernThemeManager.Theme theme : 
             com.ghostvault.ui.components.ModernThemeManager.Theme.values()) {
            if (theme.getDisplayName().equals(displayName)) {
                return theme;
            }
        }
        return com.ghostvault.ui.components.ModernThemeManager.Theme.PASSWORD_MANAGER;
    }
    
    private VBox createSecurityControls() {
        VBox controls = new VBox(15);
        
        // Session timeout
        Label timeoutLabel = new Label("Session Timeout (minutes):");
        
        sessionTimeoutSlider = new Slider(5, 120, 30);
        sessionTimeoutSlider.setShowTickLabels(true);
        sessionTimeoutSlider.setShowTickMarks(true);
        sessionTimeoutSlider.setMajorTickUnit(30);
        sessionTimeoutSlider.setMinorTickCount(5);
        sessionTimeoutSlider.setBlockIncrement(5);
        
        Label timeoutValue = new Label("30 minutes");
        sessionTimeoutSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeoutValue.setText(String.format("%.0f minutes", newVal.doubleValue()));
        });
        
        HBox timeoutBox = new HBox(10);
        timeoutBox.setAlignment(Pos.CENTER_LEFT);
        timeoutBox.getChildren().addAll(sessionTimeoutSlider, timeoutValue);
        
        // Secure delete
        secureDeleteCheckBox = new CheckBox("Enable Secure File Deletion");
        secureDeleteCheckBox.setSelected(true);
        
        Label secureDeleteInfo = new Label("Overwrite deleted files multiple times for enhanced security");
        secureDeleteInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        controls.getChildren().addAll(
            timeoutLabel, timeoutBox,
            secureDeleteCheckBox, secureDeleteInfo
        );
        
        return controls;
    }
    
    private VBox createBackupControls() {
        VBox controls = new VBox(10);
        
        autoBackupCheckBox = new CheckBox("Enable Automatic Backups");
        autoBackupCheckBox.setSelected(true);
        
        Label backupInfo = new Label("Automatically create encrypted backups of your vault");
        backupInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        controls.getChildren().addAll(autoBackupCheckBox, backupInfo);
        return controls;
    }
    
    private VBox createNotificationControls() {
        VBox controls = new VBox(10);
        
        notificationsCheckBox = new CheckBox("Enable Notifications");
        notificationsCheckBox.setSelected(true);
        
        Label notificationInfo = new Label("Show system notifications for vault operations");
        notificationInfo.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        controls.getChildren().addAll(notificationsCheckBox, notificationInfo);
        return controls;
    }
    
    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Settings(
                    darkThemeCheckBox.isSelected(),
                    autoBackupCheckBox.isSelected(),
                    (int) sessionTimeoutSlider.getValue(),
                    notificationsCheckBox.isSelected(),
                    secureDeleteCheckBox.isSelected(),
                    themeComboBox.getValue()
                );
            }
            return null;
        });
    }
    
    /**
     * Settings data class
     */
    public static class Settings {
        private final boolean darkTheme;
        private final boolean autoBackupEnabled;
        private final int sessionTimeout;
        private final boolean notificationsEnabled;
        private final boolean secureDeleteEnabled;
        private final String selectedTheme;
        
        public Settings(boolean darkTheme, boolean autoBackupEnabled, int sessionTimeout, 
                       boolean notificationsEnabled, boolean secureDeleteEnabled, String selectedTheme) {
            this.darkTheme = darkTheme;
            this.autoBackupEnabled = autoBackupEnabled;
            this.sessionTimeout = sessionTimeout;
            this.notificationsEnabled = notificationsEnabled;
            this.secureDeleteEnabled = secureDeleteEnabled;
            this.selectedTheme = selectedTheme;
        }
        
        public boolean isDarkTheme() { return darkTheme; }
        public boolean isAutoBackupEnabled() { return autoBackupEnabled; }
        public int getSessionTimeout() { return sessionTimeout; }
        public boolean isNotificationsEnabled() { return notificationsEnabled; }
        public boolean isSecureDeleteEnabled() { return secureDeleteEnabled; }
        public String getSelectedTheme() { return selectedTheme; }
        
        public com.ghostvault.ui.components.ModernThemeManager.Theme getThemeEnum() {
            if (selectedTheme == null) return com.ghostvault.ui.components.ModernThemeManager.Theme.PASSWORD_MANAGER;
            
            for (com.ghostvault.ui.components.ModernThemeManager.Theme theme : 
                 com.ghostvault.ui.components.ModernThemeManager.Theme.values()) {
                if (theme.getDisplayName().equals(selectedTheme)) {
                    return theme;
                }
            }
            return com.ghostvault.ui.components.ModernThemeManager.Theme.PASSWORD_MANAGER;
        }
    }
}
