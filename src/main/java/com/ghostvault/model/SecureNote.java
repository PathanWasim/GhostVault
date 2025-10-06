package com.ghostvault.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a secure encrypted note
 */
public class SecureNote implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private String title;
    private String content;
    private String category;
    private List<String> tags;
    private final LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    
    public SecureNote(String id, String title, String content, String category, 
                     List<String> tags, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
    
    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public List<String> getTags() { return tags; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getModifiedDate() { return modifiedDate; }
    
    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }
    
    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return "📝 " + title;
    }
    
    /**
     * Get preview text (first 100 characters)
     */
    public String getPreview() {
        if (content == null || content.isEmpty()) {
            return "Empty note";
        }
        
        String preview = content.replaceAll("\\s+", " ").trim();
        if (preview.length() > 100) {
            return preview.substring(0, 97) + "...";
        }
        return preview;
    }
    
    /**
     * Get category icon
     */
    public String getCategoryIcon() {
        switch (category.toLowerCase()) {
            case "personal": return "👤";
            case "work": return "💼";
            case "ideas": return "💡";
            case "todo": return "✅";
            case "important": return "⭐";
            case "meeting": return "🤝";
            case "project": return "📋";
            default: return "📝";
        }
    }
    
    @Override
    public String toString() {
        return title;
    }
}