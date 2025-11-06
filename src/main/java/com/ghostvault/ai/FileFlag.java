package com.ghostvault.ai;

/**
 * File flags for security and analysis warnings
 */
public enum FileFlag {
    SUSPICIOUS_EXTENSION("Suspicious Extension", "⚠️", "File has a potentially dangerous extension"),
    LARGE_SIZE("Large Size", "📏", "File is unusually large"),
    EXECUTABLE_FILE("Executable", "⚙️", "File is executable and may pose security risks"),
    ENCRYPTED_ARCHIVE("Encrypted Archive", "🔒", "Archive file that may contain encrypted content"),
    POTENTIAL_MALWARE("Potential Malware", "🦠", "File matches malware patterns"),
    SYSTEM_FILE("System File", "🔧", "System or configuration file");
    
    private final String displayName;
    private final String icon;
    private final String description;
    
    FileFlag(String displayName, String icon, String description) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}