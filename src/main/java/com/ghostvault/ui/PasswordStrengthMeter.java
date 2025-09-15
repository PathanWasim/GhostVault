package com.ghostvault.ui;

import java.util.regex.Pattern;

/**
 * Password strength meter with comprehensive validation
 * Evaluates password strength based on multiple criteria
 */
public class PasswordStrengthMeter {
    
    // Password strength levels
    public enum PasswordStrength {
        VERY_WEAK(0, "Very Weak"),
        WEAK(1, "Weak"),
        FAIR(2, "Fair"),
        GOOD(3, "Good"),
        STRONG(4, "Strong"),
        VERY_STRONG(5, "Very Strong");
        
        private final int level;
        private final String description;
        
        PasswordStrength(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    // Regex patterns for character types
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGITS = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    private static final Pattern SPACES = Pattern.compile("\\s");
    
    // Common weak passwords and patterns
    private static final String[] COMMON_PASSWORDS = {
        "password", "123456", "password123", "admin", "qwerty", "letmein",
        "welcome", "monkey", "dragon", "master", "shadow", "123456789",
        "football", "baseball", "superman", "batman", "trustno1", "hello",
        "login", "pass", "test", "guest", "user", "root", "administrator"
    };
    
    private static final String[] COMMON_PATTERNS = {
        "123", "abc", "qwe", "asd", "zxc", "111", "000", "999"
    };
    
    /**
     * Check password strength and return detailed result
     */
    public StrengthResult checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new StrengthResult(PasswordStrength.VERY_WEAK, "Password is required", 0);
        }
        
        int score = 0;
        StringBuilder feedback = new StringBuilder();
        
        // Length scoring
        int length = password.length();
        if (length < 6) {
            feedback.append("Too short (minimum 6 characters). ");
        } else if (length < 8) {
            score += 1;
            feedback.append("Short password. ");
        } else if (length < 12) {
            score += 2;
        } else if (length < 16) {
            score += 3;
        } else {
            score += 4;
        }
        
        // Character variety scoring
        int charTypes = 0;
        
        if (LOWERCASE.matcher(password).find()) {
            charTypes++;
            score += 1;
        } else {
            feedback.append("Add lowercase letters. ");
        }
        
        if (UPPERCASE.matcher(password).find()) {
            charTypes++;
            score += 1;
        } else {
            feedback.append("Add uppercase letters. ");
        }
        
        if (DIGITS.matcher(password).find()) {
            charTypes++;
            score += 1;
        } else {
            feedback.append("Add numbers. ");
        }
        
        if (SPECIAL_CHARS.matcher(password).find()) {
            charTypes++;
            score += 2; // Special characters get extra points
        } else {
            feedback.append("Add special characters (!@#$%^&*). ");
        }
        
        if (SPACES.matcher(password).find()) {
            score += 1; // Bonus for spaces (passphrases)
        }
        
        // Bonus for character variety
        if (charTypes >= 3) {
            score += 1;
        }
        if (charTypes >= 4) {
            score += 1;
        }
        
        // Check for common passwords
        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.contains(common)) {
                score -= 3;
                feedback.append("Avoid common passwords. ");
                break;
            }
        }
        
        // Check for common patterns
        for (String pattern : COMMON_PATTERNS) {
            if (lowerPassword.contains(pattern)) {
                score -= 1;
                feedback.append("Avoid common patterns. ");
                break;
            }
        }
        
        // Check for repeated characters
        if (hasRepeatedCharacters(password)) {
            score -= 1;
            feedback.append("Avoid repeated characters. ");
        }
        
        // Check for keyboard patterns
        if (hasKeyboardPattern(password)) {
            score -= 2;
            feedback.append("Avoid keyboard patterns. ");
        }
        
        // Check for dictionary words (simplified check)
        if (hasDictionaryWords(password)) {
            score -= 1;
            feedback.append("Consider using non-dictionary words. ");
        }
        
        // Ensure minimum score
        score = Math.max(0, score);
        
        // Determine strength level
        PasswordStrength strength;
        String description;
        
        if (score <= 2) {
            strength = PasswordStrength.VERY_WEAK;
            description = "Very weak - " + feedback.toString().trim();
        } else if (score <= 4) {
            strength = PasswordStrength.WEAK;
            description = "Weak - " + feedback.toString().trim();
        } else if (score <= 6) {
            strength = PasswordStrength.FAIR;
            description = "Fair - " + feedback.toString().trim();
        } else if (score <= 8) {
            strength = PasswordStrength.GOOD;
            description = "Good password";
        } else if (score <= 10) {
            strength = PasswordStrength.STRONG;
            description = "Strong password";
        } else {
            strength = PasswordStrength.VERY_STRONG;
            description = "Very strong password";
        }
        
        return new StrengthResult(strength, description, score);
    }
    
    /**
     * Check for repeated characters (3 or more in a row)
     */
    private boolean hasRepeatedCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check for keyboard patterns (qwerty, asdf, etc.)
     */
    private boolean hasKeyboardPattern(String password) {
        String[] keyboardRows = {
            "qwertyuiop", "asdfghjkl", "zxcvbnm",
            "1234567890", "!@#$%^&*()"
        };
        
        String lowerPassword = password.toLowerCase();
        
        for (String row : keyboardRows) {
            // Check for forward patterns
            for (int i = 0; i <= row.length() - 3; i++) {
                String pattern = row.substring(i, i + 3);
                if (lowerPassword.contains(pattern)) {
                    return true;
                }
            }
            
            // Check for reverse patterns
            String reversed = new StringBuilder(row).reverse().toString();
            for (int i = 0; i <= reversed.length() - 3; i++) {
                String pattern = reversed.substring(i, i + 3);
                if (lowerPassword.contains(pattern)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Simple dictionary word check
     */
    private boolean hasDictionaryWords(String password) {
        String[] commonWords = {
            "the", "and", "for", "are", "but", "not", "you", "all", "can", "had",
            "her", "was", "one", "our", "out", "day", "get", "has", "him", "his",
            "how", "man", "new", "now", "old", "see", "two", "way", "who", "boy",
            "did", "its", "let", "put", "say", "she", "too", "use", "love", "time",
            "work", "life", "home", "good", "make", "come", "know", "take", "year"
        };
        
        String lowerPassword = password.toLowerCase();
        
        for (String word : commonWords) {
            if (word.length() >= 3 && lowerPassword.contains(word)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get password strength requirements text
     */
    public static String getRequirementsText() {
        return "Password Requirements:\\n" +
               "• At least 8 characters long\\n" +
               "• Contains uppercase letters (A-Z)\\n" +
               "• Contains lowercase letters (a-z)\\n" +
               "• Contains numbers (0-9)\\n" +
               "• Contains special characters (!@#$%^&*)\\n" +
               "• Avoid common passwords and patterns\\n" +
               "• Avoid repeated characters\\n" +
               "• Avoid keyboard patterns (qwerty, 123, etc.)";
    }
    
    /**
     * Result of password strength check
     */
    public static class StrengthResult {
        private final PasswordStrength strength;
        private final String description;
        private final int score;
        
        public StrengthResult(PasswordStrength strength, String description, int score) {
            this.strength = strength;
            this.description = description;
            this.score = score;
        }
        
        public PasswordStrength getStrength() { return strength; }
        public String getDescription() { return description; }
        public int getScore() { return score; }
        
        public boolean isAcceptable() {
            return strength.getLevel() >= PasswordStrength.GOOD.getLevel();
        }
        
        public double getPercentage() {
            return Math.min(100.0, (score / 12.0) * 100.0);
        }
    }
}