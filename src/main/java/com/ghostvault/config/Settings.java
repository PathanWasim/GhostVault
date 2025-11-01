package com.ghostvault.config;

/**
 * Simple Settings class for compilation
 */
public class Settings {
    private String selectedTheme = "Default";
    private int sessionTimeout = 30;
    private boolean autoBackupEnabled = true;
    private boolean notificationsEnabled = true;
    private boolean secureDeleteEnabled = true;
    
    public String getSelectedTheme() { return selectedTheme; }
    public int getSessionTimeout() { return sessionTimeout; }
    public boolean isAutoBackupEnabled() { return autoBackupEnabled; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public boolean isSecureDeleteEnabled() { return secureDeleteEnabled; }
    
    public void setSelectedTheme(String theme) { this.selectedTheme = theme; }
    public void setSessionTimeout(int timeout) { this.sessionTimeout = timeout; }
    public void setAutoBackupEnabled(boolean enabled) { this.autoBackupEnabled = enabled; }
    public void setNotificationsEnabled(boolean enabled) { this.notificationsEnabled = enabled; }
    public void setSecureDeleteEnabled(boolean enabled) { this.secureDeleteEnabled = enabled; }
}