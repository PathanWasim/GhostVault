package com.ghostvault.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a stored password entry
 */
public class StoredPassword implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private String title;
    private String username;
    private String password;
    private String website;
    private String notes;
    private String category;
    private List<String> tags;
    private final LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    
    public StoredPassword(String id, String title, String username, String password, 
                         String website, String notes, String category, List<String> tags,
                         LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.website = website;
        this.notes = notes;
        this.category = category;
        this.tags = tags;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getWebsite() { return website; }
    public String getNotes() { return notes; }
    public String getCategory() { return category; }
    public List<String> getTags() { return tags; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    
    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setWebsite(String website) { this.website = website; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCategory(String category) { this.category = category; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return "🔑 " + title + (username != null && !username.isEmpty() ? " (" + username + ")" : "");
    }
    
    /**
     * Get masked password for display
     */
    public String getMaskedPassword() {
        if (password == null || password.isEmpty()) {
            return "";
        }
        return "●".repeat(Math.min(password.length(), 12));
    }
    
    /**
     * Get category icon
     */
    public String getCategoryIcon() {
        switch (category.toLowerCase()) {
            case "social": return "👥";
            case "email": return "📧";
            case "banking": return "🏦";
            case "shopping": return "🛒";
            case "work": return "💼";
            case "entertainment": return "🎬";
            case "gaming": return "🎮";
            case "education": return "🎓";
            case "health": return "🏥";
            case "travel": return "✈️";
            default: return "🔑";
        }
    }
    
    /**
     * Get website domain
     */
    public String getDomain() {
        if (website == null || website.isEmpty()) {
            return "";
        }
        
        try {
            String url = website.toLowerCase();
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost().replaceAll("^www\\.", "");
        } catch (Exception e) {
            return website;
        }
    }
    
    @Override
    public String toString() {
        return title;
    }
}