package com.ghostvault.security;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a stored password entry in the vault
 */
public class PasswordEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String username;
    private String password;
    private String url;
    private String notes;
    private String category;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private LocalDateTime lastUsed;
    private LocalDateTime expirationDate;
    private boolean isFavorite;
    private int strength;
    
    public PasswordEntry() {
        this.id = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
        this.isFavorite = false;
        this.strength = 0;
    }
    
    public PasswordEntry(String title, String username, String password, String url) {
        this();
        this.title = title;
        this.username = username;
        this.password = password;
        this.url = url;
        this.strength = calculatePasswordStrength(password);
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title;
        updateModifiedDate();
    }
    
    // Alias for compatibility with PasswordVaultManager
    public String getServiceName() { return title; }
    public void setServiceName(String serviceName) { setTitle(serviceName); }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username;
        updateModifiedDate();
    }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { 
        this.password = password;
        this.strength = calculatePasswordStrength(password);
        updateModifiedDate();
    }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { 
        this.url = url;
        updateModifiedDate();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes;
        updateModifiedDate();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category;
        updateModifiedDate();
    }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }
    
    // Alias for compatibility with PasswordVaultManager
    public LocalDateTime getLastModified() { return modifiedDate; }
    public void setLastModified(LocalDateTime lastModified) { setModifiedDate(lastModified); }
    
    public LocalDateTime getLastUsed() { return lastUsed; }
    public void setLastUsed(LocalDateTime lastUsed) { this.lastUsed = lastUsed; }
    
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { 
        this.expirationDate = expirationDate;
        updateModifiedDate();
    }
    
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { 
        this.isFavorite = favorite;
        updateModifiedDate();
    }
    
    public int getStrength() { return strength; }
    public void setStrength(int strength) { this.strength = strength; }
    
    private void updateModifiedDate() {
        this.modifiedDate = LocalDateTime.now();
    }
    
    public void markAsUsed() {
        this.lastUsed = LocalDateTime.now();
    }
    
    /**
     * Calculate password strength (0-100)
     */
    private int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        
        int score = 0;
        
        // Length bonus
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;
        
        // Character variety
        if (password.matches(".*[a-z].*")) score += 10;
        if (password.matches(".*[A-Z].*")) score += 10;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score += 15;
        
        return Math.min(100, score);
    }
    
    /**
     * Get password strength description
     */
    public String getStrengthDescription() {
        if (strength < 30) return "Weak";
        if (strength < 60) return "Fair";
        if (strength < 80) return "Good";
        return "Strong";
    }
    
    /**
     * Get display string for UI
     */
    public String getDisplayString() {
        String icon = getIconForCategory();
        return icon + " " + title + (username != null && !username.isEmpty() ? " - " + username : "");
    }
    
    /**
     * Get icon based on category or URL
     */
    private String getIconForCategory() {
        if (category != null) {
            switch (category.toLowerCase()) {
                case "banking": return "🏦";
                case "social": return "📱";
                case "work": return "💼";
                case "shopping": return "🛒";
                case "crypto": return "₿";
                case "email": return "📧";
                default: return "🔑";
            }
        }
        
        if (url != null) {
            String lowerUrl = url.toLowerCase();
            if (lowerUrl.contains("bank") || lowerUrl.contains("paypal")) return "🏦";
            if (lowerUrl.contains("facebook") || lowerUrl.contains("twitter") || lowerUrl.contains("instagram")) return "📱";
            if (lowerUrl.contains("amazon") || lowerUrl.contains("ebay") || lowerUrl.contains("shop")) return "🛒";
            if (lowerUrl.contains("gmail") || lowerUrl.contains("outlook") || lowerUrl.contains("mail")) return "📧";
        }
        
        return "🔑";
    }
    
    @Override
    public String toString() {
        return "PasswordEntry{" +
                "title='" + title + '\'' +
                ", username='" + username + '\'' +
                ", category='" + category + '\'' +
                ", strength=" + strength +
                '}';
    }
}