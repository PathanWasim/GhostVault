package com.ghostvault.security;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Secure password generator with customizable options
 */
public class PasswordGenerator {
    
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String AMBIGUOUS = "0O1lI";
    
    private final SecureRandom random = new SecureRandom();
    
    /**
     * Generate password with default settings
     */
    public String generatePassword() {
        return generatePassword(new PasswordGeneratorOptions());
    }
    
    /**
     * Generate password with specified length
     */
    public String generatePassword(int length) {
        return generatePassword(new PasswordGeneratorOptions().withLength(length));
    }
    
    /**
     * Generate password with custom options
     */
    public String generatePassword(PasswordGeneratorOptions options) {
        if (options.getLength() < 4) {
            throw new IllegalArgumentException("Password length must be at least 4 characters");
        }
        
        StringBuilder charset = new StringBuilder();
        List<Character> requiredChars = new ArrayList<>();
        
        // Build character set and ensure at least one character from each required type
        if (options.isIncludeLowercase()) {
            charset.append(LOWERCASE);
            requiredChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        
        if (options.isIncludeUppercase()) {
            charset.append(UPPERCASE);
            requiredChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        
        if (options.isIncludeDigits()) {
            charset.append(DIGITS);
            requiredChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        
        if (options.isIncludeSymbols()) {
            charset.append(SYMBOLS);
            requiredChars.add(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }
        
        if (charset.length() == 0) {
            throw new IllegalArgumentException("At least one character type must be selected");
        }
        
        // Remove ambiguous characters if requested
        if (options.isExcludeAmbiguous()) {
            for (char c : AMBIGUOUS.toCharArray()) {
                int index;
                while ((index = charset.indexOf(String.valueOf(c))) != -1) {
                    charset.deleteCharAt(index);
                }
            }
        }
        
        // Generate password
        List<Character> passwordChars = new ArrayList<>(requiredChars);
        
        // Fill remaining positions with random characters
        for (int i = requiredChars.size(); i < options.getLength(); i++) {
            char randomChar = charset.charAt(random.nextInt(charset.length()));
            passwordChars.add(randomChar);
        }
        
        // Shuffle to avoid predictable patterns
        Collections.shuffle(passwordChars, random);
        
        // Convert to string
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }
        
        return password.toString();
    }
    
    /**
     * Generate memorable password using word-like patterns
     */
    public String generateMemorablePassword(int wordCount, boolean includeNumbers, boolean includeSymbols) {
        String[] consonants = {"b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "z"};
        String[] vowels = {"a", "e", "i", "o", "u", "y"};
        
        StringBuilder password = new StringBuilder();
        
        for (int w = 0; w < wordCount; w++) {
            if (w > 0) {
                if (includeSymbols && random.nextBoolean()) {
                    password.append("-");
                } else if (includeNumbers) {
                    password.append(random.nextInt(10));
                }
            }
            
            // Generate word-like sequence (3-5 characters)
            int wordLength = 3 + random.nextInt(3);
            for (int i = 0; i < wordLength; i++) {
                if (i % 2 == 0) {
                    // Consonant
                    String consonant = consonants[random.nextInt(consonants.length)];
                    if (i == 0 && random.nextBoolean()) {
                        consonant = consonant.toUpperCase();
                    }
                    password.append(consonant);
                } else {
                    // Vowel
                    password.append(vowels[random.nextInt(vowels.length)]);
                }
            }
        }
        
        // Add numbers at the end if requested
        if (includeNumbers) {
            password.append(random.nextInt(90) + 10); // 2-digit number
        }
        
        return password.toString();
    }
    
    /**
     * Generate passphrase using random words
     */
    public String generatePassphrase(int wordCount, String separator) {
        // Simple word list for demonstration
        String[] words = {
            "apple", "brave", "chair", "dance", "eagle", "flame", "grace", "house",
            "image", "juice", "knife", "light", "music", "night", "ocean", "peace",
            "quiet", "river", "stone", "table", "unity", "voice", "water", "youth"
        };
        
        StringBuilder passphrase = new StringBuilder();
        
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) {
                passphrase.append(separator);
            }
            
            String word = words[random.nextInt(words.length)];
            // Capitalize first letter randomly
            if (random.nextBoolean()) {
                word = word.substring(0, 1).toUpperCase() + word.substring(1);
            }
            
            passphrase.append(word);
        }
        
        return passphrase.toString();
    }
    
    /**
     * Check if password meets complexity requirements
     */
    public boolean meetsComplexityRequirements(String password, PasswordGeneratorOptions options) {
        if (password.length() < options.getLength()) {
            return false;
        }
        
        if (options.isIncludeLowercase() && !password.matches(".*[a-z].*")) {
            return false;
        }
        
        if (options.isIncludeUppercase() && !password.matches(".*[A-Z].*")) {
            return false;
        }
        
        if (options.isIncludeDigits() && !password.matches(".*\\d.*")) {
            return false;
        }
        
        if (options.isIncludeSymbols() && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Password generator options
     */
    public static class PasswordGeneratorOptions {
        private int length = 16;
        private boolean includeLowercase = true;
        private boolean includeUppercase = true;
        private boolean includeDigits = true;
        private boolean includeSymbols = true;
        private boolean excludeAmbiguous = false;
        
        public int getLength() { return length; }
        public boolean isIncludeLowercase() { return includeLowercase; }
        public boolean isIncludeUppercase() { return includeUppercase; }
        public boolean isIncludeDigits() { return includeDigits; }
        public boolean isIncludeSymbols() { return includeSymbols; }
        public boolean isExcludeAmbiguous() { return excludeAmbiguous; }
        
        public PasswordGeneratorOptions withLength(int length) {
            this.length = length;
            return this;
        }
        
        public PasswordGeneratorOptions withLowercase(boolean include) {
            this.includeLowercase = include;
            return this;
        }
        
        public PasswordGeneratorOptions withUppercase(boolean include) {
            this.includeUppercase = include;
            return this;
        }
        
        public PasswordGeneratorOptions withDigits(boolean include) {
            this.includeDigits = include;
            return this;
        }
        
        public PasswordGeneratorOptions withSymbols(boolean include) {
            this.includeSymbols = include;
            return this;
        }
        
        public PasswordGeneratorOptions excludeAmbiguous(boolean exclude) {
            this.excludeAmbiguous = exclude;
            return this;
        }
    }
}