package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import com.ghostvault.util.FileUtils;

import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Manages password hashing, validation, and storage
 * Handles master, panic, and decoy passwords with secure encrypted storage
 */
public class PasswordManager {
    
    public enum PasswordType {
        MASTER, PANIC, DECOY, INVALID
    }
    
    /**
     * Password configuration data structure
     */
    public static class PasswordConfiguration implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final byte[] masterHash;
        private final byte[] panicHash;
        private final byte[] decoyHash;
        private final byte[] salt;
        private final int iterations;
        private final String algorithm;
        
        public PasswordConfiguration(byte[] masterHash, byte[] panicHash, byte[] decoyHash, 
                                   byte[] salt, int iterations, String algorithm) {
            this.masterHash = masterHash.clone();
            this.panicHash = panicHash.clone();
            this.decoyHash = decoyHash.clone();
            this.salt = salt.clone();
            this.iterations = iterations;
            this.algorithm = algorithm;
        }
        
        public byte[] getMasterHash() { return masterHash.clone(); }
        public byte[] getPanicHash() { return panicHash.clone(); }
        public byte[] getDecoyHash() { return decoyHash.clone(); }
        public byte[] getSalt() { return salt.clone(); }
        public int getIterations() { return iterations; }
        public String getAlgorithm() { return algorithm; }
    }
    
    private final String vaultPath;
    private final CryptoManager cryptoManager;
    private byte[] salt;
    private byte[] hashedMasterPassword;
    private byte[] hashedPanicPassword;
    private byte[] hashedDecoyPassword;
    private boolean isConfigured;
    
    public PasswordManager(String vaultPath) throws Exception {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        this.isConfigured = false;
        loadOrGenerateSalt();
        loadPasswordConfiguration();
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
     * Load password configuration from encrypted config file
     */
    private void loadPasswordConfiguration() throws Exception {
        File configFile = new File(AppConfig.CONFIG_FILE);
        
        if (configFile.exists()) {
            try {
                // Read encrypted configuration
                CryptoManager.EncryptedData encryptedConfig = FileUtils.readEncryptedFile(configFile.toPath());
                
                // Create a temporary key from salt for decryption (bootstrap key)
                SecretKey bootstrapKey = createBootstrapKey();
                byte[] decryptedData = cryptoManager.decrypt(encryptedConfig, bootstrapKey);
                
                // Deserialize configuration
                ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
                try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                    PasswordConfiguration config = (PasswordConfiguration) ois.readObject();
                    
                    hashedMasterPassword = config.getMasterHash();
                    hashedPanicPassword = config.getPanicHash();
                    hashedDecoyPassword = config.getDecoyHash();
                    
                    // Verify salt matches
                    if (!Arrays.equals(salt, config.getSalt())) {
                        throw new CryptographicException("Salt mismatch in password configuration");
                    }
                    
                    isConfigured = true;
                }
                
                // Clear sensitive data
                MemoryUtils.secureWipe(decryptedData);
                
            } catch (Exception e) {
                // If decryption fails, the config file might be corrupted or from different salt
                // Reset to unconfigured state rather than failing
                isConfigured = false;
                hashedMasterPassword = null;
                hashedPanicPassword = null;
                hashedDecoyPassword = null;
                
                // Log the error but don't throw - allow fresh initialization
                System.err.println("Warning: Could not load password configuration, will require re-initialization: " + e.getMessage());
            }
        }
    }
    
    /**
     * Create bootstrap key for encrypting/decrypting password configuration
     * Uses a deterministic key based on salt to ensure consistency
     */
    private SecretKey createBootstrapKey() throws GeneralSecurityException {
        // Use a fixed string with salt to create bootstrap key for config encryption
        // This ensures the same key is generated each time for the same salt
        String bootstrapPassword = "GhostVault-Bootstrap-Key-2024-" + Base64.getEncoder().encodeToString(salt);
        return cryptoManager.deriveKey(bootstrapPassword, salt);
    }
    
    /**
     * Initialize passwords for first-time setup with comprehensive validation
     */
    public void initializePasswords(String masterPassword, String panicPassword, String decoyPassword) throws Exception {
        char[] masterChars = null;
        char[] panicChars = null;
        char[] decoyChars = null;
        
        try {
            // Convert to char arrays for secure handling
            masterChars = masterPassword.toCharArray();
            panicChars = panicPassword.toCharArray();
            decoyChars = decoyPassword.toCharArray();
            
            // Validate password strength
            validatePasswordStrength(masterPassword, "Master", AppConfig.PASSWORD_MIN_STRENGTH);
            validatePasswordStrength(panicPassword, "Panic", 3);
            validatePasswordStrength(decoyPassword, "Decoy", 3);
            
            // Ensure passwords are different using constant-time comparison
            if (MemoryUtils.constantTimeEquals(masterChars, panicChars) ||
                MemoryUtils.constantTimeEquals(masterChars, decoyChars) ||
                MemoryUtils.constantTimeEquals(panicChars, decoyChars)) {
                throw new IllegalArgumentException("All passwords must be different from each other");
            }
            
            // Hash passwords
            hashedMasterPassword = hashPasswordToBytes(masterPassword);
            hashedPanicPassword = hashPasswordToBytes(panicPassword);
            hashedDecoyPassword = hashPasswordToBytes(decoyPassword);
            
            // Create and save encrypted configuration
            savePasswordConfiguration();
            
            isConfigured = true;
            
        } finally {
            // Clear passwords from memory
            MemoryUtils.secureWipe(masterChars, panicChars, decoyChars);
        }
    }
    
    /**
     * Validate password strength with detailed requirements
     */
    private void validatePasswordStrength(String password, String passwordType, int minStrength) {
        int strength = getPasswordStrength(password);
        if (strength < minStrength) {
            throw new IllegalArgumentException(passwordType + " password is too weak. " +
                "Current strength: " + strength + "/5, Required: " + minStrength + "/5. " +
                "Requirements: 8+ characters, uppercase, lowercase, numbers, special characters.");
        }
    }
    
    /**
     * Save encrypted password configuration to file
     */
    private void savePasswordConfiguration() throws Exception {
        try {
            // Create configuration object
            PasswordConfiguration config = new PasswordConfiguration(
                hashedMasterPassword,
                hashedPanicPassword, 
                hashedDecoyPassword,
                salt,
                AppConfig.PBKDF2_ITERATIONS,
                AppConfig.KEY_DERIVATION_ALGORITHM
            );
            
            // Serialize configuration
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(config);
            }
            
            // Encrypt configuration
            SecretKey bootstrapKey = createBootstrapKey();
            CryptoManager.EncryptedData encryptedConfig = cryptoManager.encrypt(baos.toByteArray(), bootstrapKey);
            
            // Write to file
            FileUtils.writeEncryptedFile(Paths.get(AppConfig.CONFIG_FILE), encryptedConfig);
            
            // Clear sensitive data
            MemoryUtils.secureWipe(baos.toByteArray());
            
        } catch (Exception e) {
            throw new CryptographicException("Failed to save password configuration", e);
        }
    }
    
    /**
     * Validate password and return type using constant-time comparison
     */
    public PasswordType validatePassword(String password) throws Exception {
        if (!isConfigured) {
            return PasswordType.INVALID;
        }
        
        char[] passwordChars = null;
        byte[] hashedInput = null;
        
        try {
            passwordChars = password.toCharArray();
            hashedInput = hashPasswordToBytes(password);
            
            // Use constant-time comparison to prevent timing attacks
            boolean isMaster = MemoryUtils.constantTimeEquals(hashedInput, hashedMasterPassword);
            boolean isPanic = MemoryUtils.constantTimeEquals(hashedInput, hashedPanicPassword);
            boolean isDecoy = MemoryUtils.constantTimeEquals(hashedInput, hashedDecoyPassword);
            
            // Return the first match found (order matters for security)
            if (isMaster) {
                return PasswordType.MASTER;
            } else if (isPanic) {
                return PasswordType.PANIC;
            } else if (isDecoy) {
                return PasswordType.DECOY;
            } else {
                return PasswordType.INVALID;
            }
            
        } finally {
            // Clear sensitive data from memory
            MemoryUtils.secureWipe(passwordChars);
            MemoryUtils.secureWipe(hashedInput);
        }
    }
    
    /**
     * Derive encryption key from password for vault operations
     */
    public SecretKey deriveVaultKey(String password) throws Exception {
        char[] passwordChars = null;
        
        try {
            passwordChars = password.toCharArray();
            return cryptoManager.deriveKey(password, salt);
        } finally {
            MemoryUtils.secureWipe(passwordChars);
        }
    }
    
    /**
     * Hash password using PBKDF2 and return as byte array
     */
    private byte[] hashPasswordToBytes(String password) throws Exception {
        char[] passwordChars = null;
        
        try {
            passwordChars = password.toCharArray();
            
            KeySpec spec = new PBEKeySpec(
                passwordChars,
                salt,
                AppConfig.PBKDF2_ITERATIONS,
                256
            );
            
            SecretKeyFactory factory = SecretKeyFactory.getInstance(AppConfig.KEY_DERIVATION_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
            
        } finally {
            MemoryUtils.secureWipe(passwordChars);
        }
    }
    
    /**
     * Hash password using PBKDF2 (legacy method for compatibility)
     */
    @Deprecated
    private String hashPassword(String password) throws Exception {
        byte[] hash = hashPasswordToBytes(password);
        try {
            return Base64.getEncoder().encodeToString(hash);
        } finally {
            MemoryUtils.secureWipe(hash);
        }
    }
    
    /**
     * Calculate comprehensive password strength score (0-5)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length requirements (progressive scoring)
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++; // Bonus for longer passwords
        
        // Character variety checks
        if (password.matches(".*[A-Z].*")) score++;  // Uppercase letters
        if (password.matches(".*[a-z].*")) score++;  // Lowercase letters
        if (password.matches(".*\\d.*")) score++;    // Digits
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~].*")) score++; // Special chars
        
        // Additional security checks
        if (password.length() >= 16) score++; // Extra bonus for very long passwords
        
        // Penalize common patterns
        if (isCommonPattern(password)) {
            score = Math.max(0, score - 2);
        }
        
        // Cap at 5
        return Math.min(5, score);
    }
    
    /**
     * Check for common weak password patterns
     */
    private static boolean isCommonPattern(String password) {
        String lower = password.toLowerCase();
        
        // Common weak patterns
        String[] weakPatterns = {
            "password", "123456", "qwerty", "admin", "letmein", 
            "welcome", "monkey", "dragon", "master", "shadow",
            "12345678", "abc123", "password123", "admin123"
        };
        
        for (String pattern : weakPatterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        
        // Sequential patterns
        if (lower.matches(".*(?:012|123|234|345|456|567|678|789|890|abc|bcd|cde|def).*")) {
            return true;
        }
        
        // Repeated characters
        if (password.matches(".*(.)\\1{2,}.*")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get detailed password strength feedback
     */
    public static String getPasswordStrengthFeedback(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        
        StringBuilder feedback = new StringBuilder();
        
        if (password.length() < 8) {
            feedback.append("Use at least 8 characters. ");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            feedback.append("Add uppercase letters. ");
        }
        
        if (!password.matches(".*[a-z].*")) {
            feedback.append("Add lowercase letters. ");
        }
        
        if (!password.matches(".*\\d.*")) {
            feedback.append("Add numbers. ");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~].*")) {
            feedback.append("Add special characters. ");
        }
        
        if (isCommonPattern(password)) {
            feedback.append("Avoid common words and patterns. ");
        }
        
        if (feedback.length() == 0) {
            return "Strong password!";
        }
        
        return feedback.toString().trim();
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
     * Get salt for key derivation (secure copy)
     */
    public byte[] getSalt() {
        return MemoryUtils.secureCopy(salt);
    }
    
    /**
     * Check if passwords are configured
     */
    public boolean arePasswordsConfigured() {
        return isConfigured && 
               hashedMasterPassword != null && 
               hashedPanicPassword != null && 
               hashedDecoyPassword != null;
    }
    
    /**
     * Change master password (requires current password verification)
     */
    public void changeMasterPassword(String currentPassword, String newPassword) throws Exception {
        // Verify current password
        if (validatePassword(currentPassword) != PasswordType.MASTER) {
            throw new CryptographicException("Current password verification failed");
        }
        
        // Validate new password strength
        validatePasswordStrength(newPassword, "New master", AppConfig.PASSWORD_MIN_STRENGTH);
        
        char[] newPasswordChars = null;
        try {
            newPasswordChars = newPassword.toCharArray();
            
            // Hash new password
            byte[] newHash = hashPasswordToBytes(newPassword);
            
            // Update stored hash
            MemoryUtils.secureWipe(hashedMasterPassword);
            hashedMasterPassword = newHash;
            
            // Save updated configuration
            savePasswordConfiguration();
            
        } finally {
            MemoryUtils.secureWipe(newPasswordChars);
        }
    }
    
    /**
     * Securely destroy all password data (for panic mode)
     */
    public void secureDestroy() {
        MemoryUtils.secureWipe(salt);
        MemoryUtils.secureWipe(hashedMasterPassword);
        MemoryUtils.secureWipe(hashedPanicPassword);
        MemoryUtils.secureWipe(hashedDecoyPassword);
        
        salt = null;
        hashedMasterPassword = null;
        hashedPanicPassword = null;
        hashedDecoyPassword = null;
        isConfigured = false;
        
        // Clear crypto manager
        if (cryptoManager != null) {
            cryptoManager.clearKeys();
        }
    }
    
    /**
     * Get password requirements text for UI
     */
    public static String getPasswordRequirements() {
        return "Password Requirements:\n" +
               "• At least 8 characters long\n" +
               "• Contains uppercase letters (A-Z)\n" +
               "• Contains lowercase letters (a-z)\n" +
               "• Contains numbers (0-9)\n" +
               "• Contains special characters (!@#$%^&*)\n" +
               "• Avoid common words and patterns\n" +
               "• All three passwords must be different";
    }
}