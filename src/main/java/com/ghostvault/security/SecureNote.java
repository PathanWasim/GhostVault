package com.ghostvault.security;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a secure encrypted note
 */
public class SecureNote implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private boolean isPinned;
    private boolean isEncrypted;
    private String tags;
    
    public SecureNote() {
        this.id = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
        this.modifiedDate = LocalDateTime.now();
        this.isPinned = false;
        this.isEncrypted = true;
    }
    
    public SecureNote(String title, String content) {
        this();
        this.title = title;
        this.content = content;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title;
        updateModifiedDate();
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content;
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
    
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { 
        this.isPinned = pinned;
        updateModifiedDate();
    }
    
    public boolean isEncrypted() { return isEncrypted; }
    public void setEncrypted(boolean encrypted) { this.isEncrypted = encrypted; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { 
        this.tags = tags;
        updateModifiedDate();
    }
    
    private void updateModifiedDate() {
        this.modifiedDate = LocalDateTime.now();
    }
    
    /**
     * Get display string for UI
     */
    public String getDisplayString() {
        String icon = getIconForCategory();
        String pinnedIcon = isPinned ? "📌 " : "";
        return pinnedIcon + icon + " " + (title != null && !title.isEmpty() ? title : "Untitled Note");
    }
    
    /**
     * Get icon based on category or content
     */
    private String getIconForCategory() {
        if (category != null) {
            switch (category.toLowerCase()) {
                case "personal": return "👤";
                case "work": return "💼";
                case "finance": return "💰";
                case "security": return "🔐";
                case "ideas": return "💡";
                case "recipes": return "🍳";
                case "travel": return "✈️";
                default: return "📝";
            }
        }
        
        if (content != null) {
            String lowerContent = content.toLowerCase();
            if (lowerContent.contains("password") || lowerContent.contains("login")) return "🔐";
            if (lowerContent.contains("money") || lowerContent.contains("bank")) return "💰";
            if (lowerContent.contains("idea") || lowerContent.contains("plan")) return "💡";
            if (lowerContent.contains("recipe") || lowerContent.contains("cook")) return "🍳";
        }
        
        return "📝";
    }
    
    /**
     * Get preview of content (first 100 characters)
     */
    public String getContentPreview() {
        if (content == null || content.isEmpty()) {
            return "No content";
        }
        
        String preview = content.replaceAll("\\s+", " ").trim();
        if (preview.length() > 100) {
            return preview.substring(0, 97) + "...";
        }
        return preview;
    }
    
    /**
     * Get word count
     */
    public int getWordCount() {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }
    
    /**
     * Get character count
     */
    public int getCharacterCount() {
        return content != null ? content.length() : 0;
    }
    
    @Override
    public String toString() {
        return "SecureNote{" +
                "title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", isPinned=" + isPinned +
                ", wordCount=" + getWordCount() +
                '}';
    }
}