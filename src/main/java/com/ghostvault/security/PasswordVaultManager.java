package com.ghostvault.security;

import com.ghostvault.util.FileUtils;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages encrypted password storage and retrieval
 */
public class PasswordVaultManager {
    
    private final String vaultPath;
    private final String passwordsFilePath;
    private final Map<String, PasswordEntry> passwords;
    private final CryptoManager cryptoManager;
    private SecretKey encryptionKey;
    
    public PasswordVaultManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.passwordsFilePath = vaultPath + "/passwords.enc";
        this.passwords = new HashMap<>();
        this.cryptoManager = new CryptoManager();
        
        // Ensure vault directory exists
        try {
            Files.createDirectories(Paths.get(vaultPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create vault directory", e);
        }
    }
    
    /**
     * Set encryption key for password operations
     */
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    /**
     * Load passwords from encrypted storage
     */
    public void loadPasswords() throws Exception {
        Path passwordsPath = Paths.get(passwordsFilePath);
        
        if (!Files.exists(passwordsPath)) {
            // No existing passwords file - start with empty vault
            return;
        }
        
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        try {
            // Read encrypted data
            CryptoManager.EncryptedData encrypted = FileUtils.readEncryptedFile(passwordsPath);
            
            // Decrypt passwords
            byte[] decryptedData = cryptoManager.decrypt(encrypted, encryptionKey);
            
            // Deserialize passwords
            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                @SuppressWarnings("unchecked")
                Map<String, PasswordEntry> loadedPasswords = (Map<String, PasswordEntry>) ois.readObject();
                passwords.clear();
                passwords.putAll(loadedPasswords);
            }
            
            // Clear sensitive data from memory
            Arrays.fill(decryptedData, (byte) 0);
            
        } catch (Exception e) {
            throw new Exception("Failed to load passwords: " + e.getMessage(), e);
        }
    }
    
    /**
     * Save passwords to encrypted storage
     */
    public void savePasswords() throws Exception {
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        byte[] serializedData = null;
        
        try {
            // Serialize passwords to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(new HashMap<>(passwords));
            }
            
            serializedData = baos.toByteArray();
            
            // Encrypt passwords
            byte[] encryptedBytes = cryptoManager.encrypt(serializedData, encryptionKey);
            CryptoManager.EncryptedData encrypted = CryptoManager.EncryptedData.fromCombinedData(encryptedBytes);
            
            // Write to file
            FileUtils.writeEncryptedFile(Paths.get(passwordsFilePath), encrypted);
            
        } finally {
            // Clear sensitive data from memory
            if (serializedData != null) {
                Arrays.fill(serializedData, (byte) 0);
            }
        }
    }
    
    /**
     * Add or update password entry
     */
    public void addPassword(PasswordEntry entry) throws Exception {
        passwords.put(entry.getId(), entry);
        savePasswords();
    }
    
    /**
     * Remove password entry
     */
    public void removePassword(String id) throws Exception {
        passwords.remove(id);
        savePasswords();
    }
    
    /**
     * Get password entry by ID
     */
    public PasswordEntry getPassword(String id) {
        return passwords.get(id);
    }
    
    /**
     * Get all password entries
     */
    public List<PasswordEntry> getAllPasswords() {
        return new ArrayList<>(passwords.values());
    }
    
    /**
     * Search passwords by title, username, or URL
     */
    public List<PasswordEntry> searchPasswords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPasswords();
        }
        
        String lowerQuery = query.toLowerCase();
        return passwords.values().stream()
                .filter(entry -> 
                    (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (entry.getUsername() != null && entry.getUsername().toLowerCase().contains(lowerQuery)) ||
                    (entry.getUrl() != null && entry.getUrl().toLowerCase().contains(lowerQuery)) ||
                    (entry.getCategory() != null && entry.getCategory().toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Get passwords by category
     */
    public List<PasswordEntry> getPasswordsByCategory(String category) {
        if (category == null || category.equals("All")) {
            return getAllPasswords();
        }
        
        return passwords.values().stream()
                .filter(entry -> category.equals(entry.getCategory()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get favorite passwords
     */
    public List<PasswordEntry> getFavoritePasswords() {
        return passwords.values().stream()
                .filter(PasswordEntry::isFavorite)
                .collect(Collectors.toList());
    }
    
    /**
     * Get weak passwords (strength < 60)
     */
    public List<PasswordEntry> getWeakPasswords() {
        return passwords.values().stream()
                .filter(entry -> entry.getStrength() < 60)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all categories
     */
    public Set<String> getAllCategories() {
        Set<String> categories = passwords.values().stream()
                .map(PasswordEntry::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        categories.add("All");
        return categories;
    }
    
    /**
     * Generate secure password
     */
    public String generateSecurePassword(int length, boolean includeSymbols) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        String symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        String chars = lowercase + uppercase + numbers;
        if (includeSymbols) {
            chars += symbols;
        }
        
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        
        if (includeSymbols) {
            password.append(symbols.charAt(random.nextInt(symbols.length())));
        }
        
        // Fill the rest randomly
        for (int i = password.length(); i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Shuffle the password
        List<Character> passwordChars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(passwordChars);
        
        return passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
    

    
    /**
     * Calculate password strength (0-100)
     */
    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length scoring
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 15;
        if (password.length() >= 16) score += 10;
        
        // Character variety scoring
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");
        
        if (hasLower) score += 10;
        if (hasUpper) score += 10;
        if (hasDigit) score += 10;
        if (hasSymbol) score += 15;
        
        // Bonus for having all character types
        if (hasLower && hasUpper && hasDigit && hasSymbol) {
            score += 15;
        }
        
        // Penalty for common patterns
        if (password.toLowerCase().contains("password")) score -= 20;
        if (password.toLowerCase().contains("123456")) score -= 20;
        if (password.matches(".*(..).*\\1.*")) score -= 10; // Repeated patterns
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Get password statistics
     */
    public Map<String, Object> getPasswordStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalPasswords", passwords.size());
        stats.put("favoritePasswords", getFavoritePasswords().size());
        stats.put("weakPasswords", getWeakPasswords().size());
        stats.put("categories", getAllCategories().size() - 1); // Exclude "All"
        
        // Average password strength
        double avgStrength = passwords.values().stream()
                .mapToInt(PasswordEntry::getStrength)
                .average()
                .orElse(0.0);
        stats.put("averageStrength", Math.round(avgStrength));
        
        return stats;
    }
}