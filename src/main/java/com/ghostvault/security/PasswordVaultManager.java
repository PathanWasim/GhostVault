package com.ghostvault.security;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Enhanced password vault manager with encryption, persistence, and security features
 */
public class PasswordVaultManager {
    private SecretKey encryptionKey;
    private List<PasswordEntry> passwords = new ArrayList<>();
    private final String vaultPath;
    private final String passwordsFile;
    private final SecureRandom secureRandom = new SecureRandom();
    private ScheduledExecutorService clipboardCleaner;
    
    public PasswordVaultManager(String vaultPath) {
        this.vaultPath = vaultPath;
        this.passwordsFile = vaultPath + File.separator + "passwords.enc";
        
        // Ensure vault directory exists
        try {
            Files.createDirectories(Paths.get(vaultPath));
        } catch (Exception e) {
            System.err.println("Failed to create vault directory: " + e.getMessage());
        }
        
        // Initialize clipboard cleaner
        this.clipboardCleaner = Executors.newSingleThreadScheduledExecutor();
    }
    
    public void setEncryptionKey(SecretKey key) {
        this.encryptionKey = key;
    }
    
    public void loadPasswords() throws Exception {
        loadData();
    }
    
    public void loadData() throws Exception {
        File file = new File(passwordsFile);
        if (!file.exists()) {
            // No existing password file, start with empty list
            passwords.clear();
            return;
        }
        
        if (encryptionKey == null) {
            throw new IllegalStateException("Encryption key not set");
        }
        
        try {
            // Read encrypted data
            byte[] encryptedData = Files.readAllBytes(file.toPath());
            
            if (encryptedData.length < 12) {
                throw new Exception("Invalid password file format");
            }
            
            // Extract IV (first 12 bytes)
            byte[] iv = new byte[12];
            System.arraycopy(encryptedData, 0, iv, 0, 12);
            
            // Extract encrypted content
            byte[] encrypted = new byte[encryptedData.length - 12];
            System.arraycopy(encryptedData, 12, encrypted, 0, encrypted.length);
            
            // Decrypt data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, gcmSpec);
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            // Deserialize password list
            try (ObjectInputStream ois = new ObjectInputStream(new java.io.ByteArrayInputStream(decryptedData))) {
                @SuppressWarnings("unchecked")
                List<PasswordEntry> loadedPasswords = (List<PasswordEntry>) ois.readObject();
                passwords.clear();
                passwords.addAll(loadedPasswords);
            }
            
        } catch (Exception e) {
            if (e.getMessage().contains("Tag mismatch")) {
                // Encryption key mismatch - likely different session or corrupted file
                System.err.println("Password file encryption key mismatch. Starting with empty password list.");
                passwords.clear();
                // Backup the corrupted file
                try {
                    String backupFile = passwordsFile + ".backup." + System.currentTimeMillis();
                    Files.copy(Paths.get(passwordsFile), Paths.get(backupFile));
                    Files.delete(Paths.get(passwordsFile));
                    System.out.println("Corrupted password file backed up to: " + backupFile);
                } catch (Exception backupEx) {
                    System.err.println("Could not backup corrupted file: " + backupEx.getMessage());
                }
                return;
            }
            throw new Exception("Failed to load passwords: " + e.getMessage(), e);
        }
    }
    
    public List<PasswordEntry> getAllPasswords() {
        return new ArrayList<>(passwords);
    }
    
    public List<PasswordEntry> searchPasswords(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(passwords);
        }
        
        String searchQuery = query.toLowerCase().trim();
        return passwords.stream()
            .filter(entry -> 
                entry.getServiceName().toLowerCase().contains(searchQuery) ||
                entry.getUsername().toLowerCase().contains(searchQuery) ||
                (entry.getNotes() != null && entry.getNotes().toLowerCase().contains(searchQuery))
            )
            .collect(Collectors.toList());
    }
    
    public int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length scoring
        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 10;
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
        
        // Bonus for all character types
        if (hasLower && hasUpper && hasDigit && hasSymbol) score += 15;
        
        // Penalty for common patterns
        if (password.matches(".*123.*") || password.matches(".*abc.*") || 
            password.toLowerCase().contains("password") || 
            password.toLowerCase().contains("admin")) {
            score -= 20;
        }
        
        // Penalty for repeated characters
        if (password.matches(".*(.)\\1{2,}.*")) {
            score -= 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }
    
    public void addPassword(PasswordEntry entry) throws Exception {
        if (entry == null) {
            throw new IllegalArgumentException("Password entry cannot be null");
        }
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedDate(now);
        entry.setLastModified(now);
        
        // Generate ID if not set
        if (entry.getId() == null || entry.getId().isEmpty()) {
            entry.setId(generatePasswordId());
        }
        
        passwords.add(entry);
        System.out.println("🔑 Adding password: " + entry.getServiceName() + " (Total passwords: " + passwords.size() + ")");
        System.out.println("🔑 Password ID: " + entry.getId());
        System.out.println("🔑 Encryption key available: " + (encryptionKey != null));
        savePasswords();
        System.out.println("💾 Password saved to file: " + passwordsFile);
        
        // Verify save by checking file exists and has content
        File file = new File(passwordsFile);
        if (file.exists()) {
            System.out.println("✅ Passwords file exists, size: " + file.length() + " bytes");
        } else {
            System.out.println("❌ Passwords file does not exist after save!");
        }
    }
    
    public void updatePassword(PasswordEntry entry) throws Exception {
        if (entry == null || entry.getId() == null) {
            throw new IllegalArgumentException("Invalid password entry");
        }
        
        // Find and update existing entry
        for (int i = 0; i < passwords.size(); i++) {
            if (passwords.get(i).getId().equals(entry.getId())) {
                entry.setLastModified(LocalDateTime.now());
                passwords.set(i, entry);
                savePasswords();
                return;
            }
        }
        
        throw new Exception("Password entry not found: " + entry.getId());
    }
    
    public void deletePassword(String id) throws Exception {
        passwords.removeIf(p -> p.getId().equals(id));
    }
    
    public void removePassword(String id) throws Exception {
        passwords.removeIf(p -> p.getId().equals(id));
    }
    
    public void savePasswords() throws Exception {
        if (encryptionKey == null) {
            System.out.println("❌ Cannot save passwords: Encryption key not set");
            throw new IllegalStateException("Encryption key not set");
        }
        
        System.out.println("💾 Saving " + passwords.size() + " passwords to: " + passwordsFile);
        System.out.println("💾 Encryption key algorithm: " + encryptionKey.getAlgorithm());
        
        try {
            // Ensure directory exists
            Files.createDirectories(Paths.get(passwordsFile).getParent());
            
            // Serialize password list
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(passwords);
            }
            byte[] serializedData = baos.toByteArray();
            System.out.println("💾 Serialized data size: " + serializedData.length + " bytes");
            
            // Generate random IV
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            
            // Encrypt data
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, gcmSpec);
            byte[] encryptedData = cipher.doFinal(serializedData);
            System.out.println("💾 Encrypted data size: " + encryptedData.length + " bytes");
            
            // Combine IV and encrypted data
            byte[] finalData = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, finalData, 0, iv.length);
            System.arraycopy(encryptedData, 0, finalData, iv.length, encryptedData.length);
            
            // Write to file
            Files.write(Paths.get(passwordsFile), finalData);
            System.out.println("💾 Final file size: " + finalData.length + " bytes");
            
            // Verify file was written
            File file = new File(passwordsFile);
            if (file.exists() && file.length() > 0) {
                System.out.println("✅ Passwords saved successfully, file size: " + file.length() + " bytes");
            } else {
                System.out.println("❌ Passwords file verification failed!");
                throw new Exception("File verification failed after save");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Failed to save passwords: " + e.getMessage());
            e.printStackTrace();
            
            com.ghostvault.logging.SystemErrorLog errorLog = new com.ghostvault.logging.SystemErrorLog(
                "PasswordVaultManager", "savePasswords", e, "Failed to save passwords to encrypted file");
            errorLog.logToConsole();
            
            throw new Exception("Failed to save passwords: " + e.getMessage(), e);
        }
    }
    
    public java.util.Map<String, Object> getPasswordStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", passwords.size());
        
        // Calculate strength distribution
        int weak = 0, fair = 0, good = 0, strong = 0, excellent = 0;
        
        for (PasswordEntry entry : passwords) {
            int strength = calculatePasswordStrength(entry.getPassword());
            if (strength < 20) weak++;
            else if (strength < 40) fair++;
            else if (strength < 60) good++;
            else if (strength < 80) strong++;
            else excellent++;
        }
        
        stats.put("weak", weak);
        stats.put("fair", fair);
        stats.put("good", good);
        stats.put("strong", strong);
        stats.put("excellent", excellent);
        
        // Expiring passwords (within 30 days)
        long expiring = passwords.stream()
            .filter(p -> p.getExpirationDate() != null)
            .filter(p -> p.getExpirationDate().isBefore(LocalDateTime.now().plusDays(30)))
            .count();
        stats.put("expiring", expiring);
        
        return stats;
    }
    
    public String generateSecurePassword(int length, boolean includeSymbols) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (includeSymbols) {
            chars += "!@#$%^&*()_+-=[]{}|;:,.<>?";
        }
        
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    
    /**
     * Generate a unique ID for password entries
     */
    private String generatePasswordId() {
        return "pwd_" + System.currentTimeMillis() + "_" + secureRandom.nextInt(10000);
    }
    
    /**
     * Schedule password expiration for a specific entry
     */
    public void schedulePasswordExpiration(String passwordId, LocalDateTime expiration) throws Exception {
        for (PasswordEntry entry : passwords) {
            if (entry.getId().equals(passwordId)) {
                entry.setExpirationDate(expiration);
                savePasswords();
                return;
            }
        }
        throw new Exception("Password entry not found: " + passwordId);
    }
    
    /**
     * Get passwords expiring within specified days
     */
    public List<PasswordEntry> getExpiringPasswords(int daysAhead) {
        LocalDateTime cutoff = LocalDateTime.now().plusDays(daysAhead);
        return passwords.stream()
            .filter(p -> p.getExpirationDate() != null)
            .filter(p -> p.getExpirationDate().isBefore(cutoff))
            .collect(Collectors.toList());
    }
    
    /**
     * Copy password to clipboard with automatic clearing
     */
    public void copyPasswordToClipboard(String passwordId, int clearAfterSeconds) {
        for (PasswordEntry entry : passwords) {
            if (entry.getId().equals(passwordId)) {
                // Copy to clipboard
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(entry.getPassword());
                clipboard.setContent(content);
                
                // Schedule clipboard clearing
                clipboardCleaner.schedule(() -> {
                    javafx.scene.input.ClipboardContent clearContent = new javafx.scene.input.ClipboardContent();
                    clearContent.putString("");
                    clipboard.setContent(clearContent);
                }, clearAfterSeconds, TimeUnit.SECONDS);
                
                return;
            }
        }
    }
    
    /**
     * Get password strength analysis with suggestions
     */
    public PasswordStrengthAnalysis analyzePasswordStrength(String password) {
        int score = calculatePasswordStrength(password);
        List<String> suggestions = new ArrayList<>();
        
        if (password == null || password.length() < 8) {
            suggestions.add("Use at least 8 characters");
        }
        if (!password.matches(".*[a-z].*")) {
            suggestions.add("Add lowercase letters");
        }
        if (!password.matches(".*[A-Z].*")) {
            suggestions.add("Add uppercase letters");
        }
        if (!password.matches(".*\\d.*")) {
            suggestions.add("Add numbers");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) {
            suggestions.add("Add special characters");
        }
        if (password != null && password.length() < 12) {
            suggestions.add("Consider using 12+ characters for better security");
        }
        
        String level;
        if (score < 20) level = "Very Weak";
        else if (score < 40) level = "Weak";
        else if (score < 60) level = "Fair";
        else if (score < 80) level = "Good";
        else level = "Excellent";
        
        return new PasswordStrengthAnalysis(score, level, suggestions);
    }
    
    /**
     * Cleanup resources
     */
    public void shutdown() {
        if (clipboardCleaner != null && !clipboardCleaner.isShutdown()) {
            clipboardCleaner.shutdown();
        }
    }
    
    /**
     * Password strength analysis result
     */
    public static class PasswordStrengthAnalysis {
        private final int score;
        private final String level;
        private final List<String> suggestions;
        
        public PasswordStrengthAnalysis(int score, String level, List<String> suggestions) {
            this.score = score;
            this.level = level;
            this.suggestions = suggestions;
        }
        
        public int getScore() { return score; }
        public String getLevel() { return level; }
        public List<String> getSuggestions() { return suggestions; }
    }
}