package com.ghostvault.ai;

/**
 * Security risk levels for file analysis
 */
public enum SecurityRisk {
    SAFE("Safe", "🟢", "#10B981"),
    LOW("Low Risk", "🟡", "#F59E0B"),
    MEDIUM("Medium Risk", "🟠", "#F97316"),
    HIGH("High Risk", "🔴", "#EF4444"),
    CRITICAL("Critical Risk", "⚫", "#7C2D12");
    
    private final String displayName;
    private final String icon;
    private final String color;
    
    SecurityRisk(String displayName, String icon, String color) {
        this.displayName = displayName;
        this.icon = icon;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getColor() {
        return color;
    }
    
    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}