package com.ghostvault.ai;

/**
 * File categories for AI classification
 */
public enum FileCategory {
    DOCUMENT("Document", "📄"),
    IMAGE("Image", "🖼️"),
    VIDEO("Video", "🎥"),
    AUDIO("Audio", "🎵"),
    ARCHIVE("Archive", "📦"),
    EXECUTABLE("Executable", "⚙️"),
    CODE("Code", "💻"),
    OTHER("Other", "📋");
    
    private final String displayName;
    private final String icon;
    
    FileCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}