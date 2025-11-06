package com.ghostvault.security;

import java.util.List;
import java.util.ArrayList;

/**
 * Password strength analysis with scoring and suggestions
 */
public class PasswordStrength {
    private final int score;          // 0-100
    private final String level;       // Weak, Fair, Good, Strong, Excellent
    private final List<String> suggestions;
    
    public PasswordStrength(int score, String level, List<String> suggestions) {
        this.score = Math.max(0, Math.min(100, score));
        this.level = level;
        this.suggestions = suggestions != null ? new ArrayList<>(suggestions) : new ArrayList<>();
    }
    
    public int getScore() {
        return score;
    }
    
    public String getLevel() {
        return level;
    }
    
    public List<String> getSuggestions() {
        return new ArrayList<>(suggestions);
    }
    
    public boolean isWeak() {
        return score < 40;
    }
    
    public boolean isFair() {
        return score >= 40 && score < 60;
    }
    
    public boolean isGood() {
        return score >= 60 && score < 80;
    }
    
    public boolean isStrong() {
        return score >= 80;
    }
    
    public String getColorCode() {
        if (score < 20) return "#EF4444"; // Red
        if (score < 40) return "#F97316"; // Orange
        if (score < 60) return "#F59E0B"; // Yellow
        if (score < 80) return "#10B981"; // Green
        return "#059669"; // Dark Green
    }
    
    public String getProgressBarStyle() {
        return String.format("-fx-accent: %s; -fx-control-inner-background: %s;", 
                           getColorCode(), getColorCode());
    }
    
    /**
     * Create PasswordStrength from analysis
     */
    public static PasswordStrength analyze(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(0, "No Password", 
                List.of("Please enter a password"));
        }
        
        int score = 0;
        List<String> suggestions = new ArrayList<>();
        
        // Length scoring
        if (password.length() >= 8) {
            score += 20;
        } else {
            suggestions.add("Use at least 8 characters");
        }
        
        if (password.length() >= 12) {
            score += 10;
        } else if (password.length() >= 8) {
            suggestions.add("Consider using 12+ characters for better security");
        }
        
        if (password.length() >= 16) {
            score += 10;
        }
        
        // Character variety scoring
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
        
        if (hasLower) {
            score += 10;
        } else {
            suggestions.add("Add lowercase letters (a-z)");
        }
        
        if (hasUpper) {
            score += 10;
        } else {
            suggestions.add("Add uppercase letters (A-Z)");
        }
        
        if (hasDigit) {
            score += 10;
        } else {
            suggestions.add("Add numbers (0-9)");
        }
        
        if (hasSymbol) {
            score += 15;
        } else {
            suggestions.add("Add special characters (!@#$%^&*)");
        }
        
        // Bonus for all character types
        if (hasLower && hasUpper && hasDigit && hasSymbol) {
            score += 15;
        }
        
        // Penalty for common patterns
        if (password.matches(".*123.*") || password.matches(".*abc.*") || 
            password.toLowerCase().contains("password") || 
            password.toLowerCase().contains("admin") ||
            password.toLowerCase().contains("qwerty")) {
            score -= 20;
            suggestions.add("Avoid common patterns like '123', 'abc', 'password'");
        }
        
        // Penalty for repeated characters
        if (password.matches(".*(.)\\1{2,}.*")) {
            score -= 10;
            suggestions.add("Avoid repeating the same character multiple times");
        }
        
        // Determine level
        String level;
        if (score < 20) level = "Very Weak";
        else if (score < 40) level = "Weak";
        else if (score < 60) level = "Fair";
        else if (score < 80) level = "Good";
        else level = "Excellent";
        
        // Add positive feedback for strong passwords
        if (score >= 80 && suggestions.isEmpty()) {
            suggestions.add("Excellent! This is a very strong password.");
        }
        
        return new PasswordStrength(score, level, suggestions);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%d/100)", level, score);
    }
}