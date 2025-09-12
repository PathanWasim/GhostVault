package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import com.ghostvault.util.FileUtils;

import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Manages password hashing, validation, and storage
 * Handles master, panic, and decoy passwords
 */
public class PasswordManager {
    
    public enum PasswordType {
        MASTER, PANIC, DECOY, INVALID
    }
    
    private final String vaultPath;
    private byte[] salt;
    private String hashedMasterPassword;
    private String hashedPanicPassword;
    private String hashedDecoyPassword;
    
    public PasswordManager(String vaultPath) throws Exception {
        this.vaultPath = vaultPath;
        loadOrGenerateSalt();
        loadPasswordHashes();
    }
    
    /**
     * Load existing salt or generate new one
     */
    private void loadOrGenerateSalt() throws Exception {
        File saltFile = new File(AppConfig.SALT_FILE);
        
        if (saltFile.exists()) {
            salt = Files.readAllBytes(saltFile.toPath());
        } else {
            // Generate new salt
            salt = new byte[AppConfig.SALT_SIZE];
            new SecureRandom().nextBytes(salt);
            
            // Save salt to file
            Files.write(Paths.get(AppConfig.SALT_FILE), salt);
            
            // Hide salt file on Windows
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                try {
                    Files.setAttribute(Paths.get(AppConfig.SALT_FILE), "dos:hidden", true);
                } catch (Exception e) {
                    // Ignore if hiding fails
                }
            }
        }
    }
    
    /**
     * Load password hashes from config file
     */
    private void loadPasswordHashes() throws Exception {
        File configFile = new File(AppConfig.CONFIG_FILE);
        
        if (configFile.exists()) {
            // For initial implementation, we'll use a simple format
            // In production, this should be encrypted
            byte[] configData = Files.readAllBytes(configFile.toPath());
            String configContent = new String(configData);
            
            String[] parts = configContent.split("\\|");
            if (parts.length >= 3) {
                hashedMasterPassword = parts[0];
                hashedPanicPassword = parts[1];
                hashedDecoyPassword = parts[2];
            }
        }
    }
    
    /**
     * Initialize passwords for first-time setup
     */
    public void initializePasswords(String masterPassword, String panicPassword, String decoyPassword) throws Exception {
        // Validate password strength
        if (getPasswordStrength(masterPassword) < AppConfig.PASSWORD_MIN_STRENGTH) {
            throw new IllegalArgumentException("Master password is too weak");
        }
        
        if (getPasswordStrength(panicPassword) < 3) {
            throw new IllegalArgumentException("Panic password is too weak");
        }
        
        if (getPasswordStrength(decoyPassword) < 3) {
            throw new IllegalArgumentException("Decoy password is too weak");
        }
        
        // Ensure passwords are different
        if (masterPassword.equals(panicPassword) || 
            masterPassword.equals(decoyPassword) || 
            panicPassword.equals(decoyPassword)) {
            throw new IllegalArgumentException("All passwords must be different");
        }
        
        // Hash passwords
        hashedMasterPassword = hashPassword(masterPassword);
        hashedPanicPassword = hashPassword(panicPassword);
        hashedDecoyPassword = hashPassword(decoyPassword);
        
        // Save to config file (encrypted in production)
        String configContent = hashedMasterPassword + "|" + hashedPanicPassword + "|" + hashedDecoyPassword;
        Files.write(Paths.get(AppConfig.CONFIG_FILE), configContent.getBytes());
        
        // Clear passwords from memory
        clearPassword(masterPassword);
        clearPassword(panicPassword);
        clearPassword(decoyPassword);
    }
    
    /**
     * Validate password and return type
     */
    public PasswordType validatePassword(String password) throws Exception {
        String hashedInput = hashPassword(password);
        
        // Clear password from memory immediately
        clearPassword(password);
        
        if (hashedInput.equals(hashedMasterPassword)) {
            return PasswordType.MASTER;
        } else if (hashedInput.equals(hashedPanicPassword)) {
            return PasswordType.PANIC;
        } else if (hashedInput.equals(hashedDecoyPassword)) {
            return PasswordType.DECOY;
        } else {
            return PasswordType.INVALID;
        }
    }
    
    /**
     * Hash password using PBKDF2
     */
    private String hashPassword(String password) throws Exception {
        KeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            AppConfig.PBKDF2_ITERATIONS,
            256
        );
        
        SecretKeyFactory factory = SecretKeyFactory.getInstance(AppConfig.KEY_DERIVATION_ALGORITHM);
        byte[] hash = factory.generateSecret(spec).getEncoded();
        
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * Calculate password strength score (1-5)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        
        // Character variety checks
        if (password.matches(".*[A-Z].*")) score++;  // Uppercase
        if (password.matches(".*[a-z].*")) score++;  // Lowercase
        if (password.matches(".*\\d.*")) score++;    // Digits
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++; // Special chars
        
        return score;
    }
    
    /**
     * Get password strength description
     */
    public static String getPasswordStrengthDescription(int score) {
        switch (score) {
            case 0: return "";
            case 1: return "Very Weak";
            case 2: return "Weak";
            case 3: return "Fair";
            case 4: return "Strong";
            case 5: return "Very Strong";
            default: return "Unknown";
        }
    }
    
    /**
     * Get password strength color for UI
     */
    public static String getPasswordStrengthColor(int score) {
        switch (score) {
            case 1: return "#f44336"; // Red
            case 2: return "#ff9800"; // Orange
            case 3: return "#ffeb3b"; // Yellow
            case 4: return "#8bc34a"; // Light Green
            case 5: return "#4caf50"; // Green
            default: return "#cccccc"; // Gray
        }
    }
    
    /**
     * Clear password string from memory
     */
    private void clearPassword(String password) {
        if (password != null) {
            char[] chars = password.toCharArray();
            Arrays.fill(chars, '\0');
        }
    }
    
    /**
     * Get salt for key derivation
     */
    public byte[] getSalt() {
        return salt.clone();
    }
    
    /**
     * Check if passwords are configured
     */
    public boolean arePasswordsConfigured() {
        return hashedMasterPassword != null && 
               hashedPanicPassword != null && 
               hashedDecoyPassword != null;
    }
}