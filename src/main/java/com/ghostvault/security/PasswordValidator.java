package com.ghostvault.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Comprehensive password validation utility
 * Provides detailed validation rules and feedback for password security
 */
public class PasswordValidator {
    
    // Password validation patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~].*");
    private static final Pattern SEQUENTIAL_PATTERN = Pattern.compile(".*(?:012|123|234|345|456|567|678|789|890|abc|bcd|cde|def).*");
    private static final Pattern REPEATED_CHAR_PATTERN = Pattern.compile(".*(.)\\1{2,}.*");
    
    // Common weak passwords and patterns
    private static final String[] COMMON_WEAK_PASSWORDS = {
        "password", "123456", "123456789", "12345678", "12345", "1234567", "1234567890",
        "qwerty", "abc123", "password123", "admin", "letmein", "welcome", "monkey",
        "dragon", "master", "shadow", "superman", "michael", "football", "baseball",
        "liverpool", "jordan", "princess", "charlie", "aa123456", "donald", "password1",
        "guest", "1234", "a1b2c3", "123123", "lovely", "iloveyou", "babygirl", "princess1"
    };
    
    /**
     * Validation result containing score and detailed feedback
     */
    public static class ValidationResult {
        private final int score;
        private final List<String> issues;
        private final List<String> suggestions;
        private final boolean isValid;
        
        public ValidationResult(int score, List<String> issues, List<String> suggestions, boolean isValid) {
            this.score = score;
            this.issues = new ArrayList<>(issues);
            this.suggestions = new ArrayList<>(suggestions);
            this.isValid = isValid;
        }
        
        public int getScore() { return score; }
        public List<String> getIssues() { return new ArrayList<>(issues); }
        public List<String> getSuggestions() { return new ArrayList<>(suggestions); }
        public boolean isValid() { return isValid; }
        
        public String getStrengthDescription() {
            switch (score) {
                case 0: return "No Password";
                case 1: return "Very Weak";
                case 2: return "Weak";
                case 3: return "Fair";
                case 4: return "Strong";
                case 5: return "Very Strong";
                default: return "Unknown";
            }
        }
        
        public String getStrengthColor() {
            switch (score) {
                case 0: return "#cccccc"; // Gray
                case 1: return "#f44336"; // Red
                case 2: return "#ff9800"; // Orange
                case 3: return "#ffeb3b"; // Yellow
                case 4: return "#8bc34a"; // Light Green
                case 5: return "#4caf50"; // Green
                default: return "#cccccc"; // Gray
            }
        }
    }
    
    /**
     * Comprehensive password validation with detailed feedback
     */
    public static ValidationResult validatePassword(String password, int minimumScore) {
        List<String> issues = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        int score = 0;
        
        if (password == null || password.isEmpty()) {
            issues.add("Password is required");
            suggestions.add("Enter a password");
            return new ValidationResult(0, issues, suggestions, false);
        }
        
        // Length checks
        if (password.length() < 8) {
            issues.add("Password is too short (minimum 8 characters)");
            suggestions.add("Use at least 8 characters");
        } else {
            score++;
            if (password.length() >= 12) {
                score++; // Bonus for longer passwords
            }
        }
        
        // Character variety checks
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            issues.add("Missing uppercase letters");
            suggestions.add("Add uppercase letters (A-Z)");
        } else {
            score++;
        }
        
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            issues.add("Missing lowercase letters");
            suggestions.add("Add lowercase letters (a-z)");
        } else {
            score++;
        }
        
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            issues.add("Missing numbers");
            suggestions.add("Add numbers (0-9)");
        } else {
            score++;
        }
        
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            issues.add("Missing special characters");
            suggestions.add("Add special characters (!@#$%^&*)");
        } else {
            score++;
        }
        
        // Additional security checks
        if (password.length() >= 16) {
            score++; // Extra bonus for very long passwords
        }
        
        // Check for common weak patterns
        String lowerPassword = password.toLowerCase();
        
        // Common weak passwords
        for (String weakPassword : COMMON_WEAK_PASSWORDS) {
            if (lowerPassword.contains(weakPassword)) {
                issues.add("Contains common weak password pattern: " + weakPassword);
                suggestions.add("Avoid common words and patterns");
                score = Math.max(0, score - 2);
                break;
            }
        }
        
        // Sequential patterns
        if (SEQUENTIAL_PATTERN.matcher(lowerPassword).matches()) {
            issues.add("Contains sequential characters");
            suggestions.add("Avoid sequential patterns like 123 or abc");
            score = Math.max(0, score - 1);
        }
        
        // Repeated characters
        if (REPEATED_CHAR_PATTERN.matcher(password).matches()) {
            issues.add("Contains repeated characters");
            suggestions.add("Avoid repeating the same character multiple times");
            score = Math.max(0, score - 1);
        }
        
        // Dictionary word check (basic)
        if (containsCommonWords(lowerPassword)) {
            issues.add("Contains common dictionary words");
            suggestions.add("Use less common words or combine multiple words");
            score = Math.max(0, score - 1);
        }
        
        // Cap score at 5
        score = Math.min(5, score);
        
        // Determine if password meets minimum requirements
        boolean isValid = score >= minimumScore && issues.stream().noneMatch(issue -> 
            issue.contains("too short") || 
            issue.contains("Missing uppercase") || 
            issue.contains("Missing lowercase") || 
            issue.contains("Missing numbers") || 
            issue.contains("Missing special"));
        
        return new ValidationResult(score, issues, suggestions, isValid);
    }
    
    /**
     * Quick password strength check (compatible with existing code)
     */
    public static int getPasswordStrength(String password) {
        return validatePassword(password, 0).getScore();
    }
    
    /**
     * Check if password contains common dictionary words
     */
    private static boolean containsCommonWords(String password) {
        String[] commonWords = {
            "love", "hate", "good", "bad", "best", "worst", "happy", "sad",
            "big", "small", "fast", "slow", "hot", "cold", "new", "old",
            "black", "white", "red", "blue", "green", "yellow", "orange",
            "cat", "dog", "bird", "fish", "tree", "flower", "house", "car"
        };
        
        for (String word : commonWords) {
            if (password.contains(word)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generate password requirements text
     */
    public static String getPasswordRequirements(int minimumScore) {
        StringBuilder requirements = new StringBuilder();
        requirements.append("Password Requirements:\n");
        requirements.append("• At least 8 characters long\n");
        requirements.append("• Contains uppercase letters (A-Z)\n");
        requirements.append("• Contains lowercase letters (a-z)\n");
        requirements.append("• Contains numbers (0-9)\n");
        requirements.append("• Contains special characters (!@#$%^&*)\n");
        requirements.append("• Avoid common words and patterns\n");
        requirements.append("• Avoid sequential or repeated characters\n");
        requirements.append("• Minimum strength score: ").append(minimumScore).append("/5");
        
        return requirements.toString();
    }
    
    /**
     * Check if passwords are sufficiently different from each other
     */
    public static boolean arePasswordsSufficientlyDifferent(String password1, String password2, String password3) {
        if (password1 == null || password2 == null || password3 == null) {
            return false;
        }
        
        // Check exact matches
        if (password1.equals(password2) || password1.equals(password3) || password2.equals(password3)) {
            return false;
        }
        
        // Check similarity (basic Levenshtein distance check)
        if (calculateSimilarity(password1, password2) > 0.8 ||
            calculateSimilarity(password1, password3) > 0.8 ||
            calculateSimilarity(password2, password3) > 0.8) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate similarity between two strings (0.0 = completely different, 1.0 = identical)
     */
    private static double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLength;
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    private PasswordValidator() {
        // Utility class - prevent instantiation
    }
}