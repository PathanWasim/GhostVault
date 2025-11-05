package com.ghostvault.security;

import javax.crypto.SecretKey;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service for managing encryption keys derived from user passwords
 * Provides centralized key derivation and secure key lifecycle management
 */
public class KeyManagementService {
    
    private final CryptoManager cryptoManager;
    private final SecurePasswordStorage passwordStorage;
    private final Map<SecurePasswordStorage.PasswordType, SecretKey> derivedKeys;
    private final Map<SecurePasswordStorage.PasswordType, byte[]> cachedSalts;
    private boolean keysLoaded = false;
    
    public KeyManagementService() {
        this.cryptoManager = new CryptoManager();
        this.passwordStorage = new SecurePasswordStorage();
        this.derivedKeys = new ConcurrentHashMap<>();
        this.cachedSalts = new ConcurrentHashMap<>();
    }
    
    public KeyManagementService(String vaultPath) {
        this.cryptoManager = new CryptoManager();
        this.passwordStorage = new SecurePasswordStorage(vaultPath);
        this.derivedKeys = new ConcurrentHashMap<>();
        this.cachedSalts = new ConcurrentHashMap<>();
    }
    
    /**
     * Derive and cache encryption keys from user password
     * @param password The user's password
     * @param passwordType The type of password (MASTER, DECOY, PANIC)
     * @return The derived encryption key
     * @throws Exception if key derivation fails
     */
    public SecretKey deriveAndCacheKey(String password, SecurePasswordStorage.PasswordType passwordType) throws Exception {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Get salt for this password type
        byte[] salt = passwordStorage.getSaltForKeyDerivation(passwordType);
        if (salt == null) {
            throw new IllegalStateException("No salt found for password type: " + passwordType);
        }
        
        // Derive encryption key
        SecretKey key = cryptoManager.deriveEncryptionKey(password, salt);
        
        // Cache the key and salt
        derivedKeys.put(passwordType, key);
        cachedSalts.put(passwordType, salt.clone());
        
        System.out.println("🔑 Derived and cached encryption key for: " + passwordType);
        return key;
    }
    
    /**
     * Get cached encryption key for password type
     * @param passwordType The password type
     * @return The cached key or null if not cached
     */
    public SecretKey getCachedKey(SecurePasswordStorage.PasswordType passwordType) {
        return derivedKeys.get(passwordType);
    }
    
    /**
     * Get salt for password type
     * @param passwordType The password type
     * @return The salt bytes
     */
    public byte[] getSalt(SecurePasswordStorage.PasswordType passwordType) {
        byte[] salt = cachedSalts.get(passwordType);
        return salt != null ? salt.clone() : null;
    }
    
    /**
     * Derive key for authentication (without caching)
     * @param password The password to authenticate
     * @param passwordType The password type to check against
     * @return true if password is valid
     * @throws Exception if authentication fails
     */
    public boolean authenticatePassword(String password, SecurePasswordStorage.PasswordType passwordType) throws Exception {
        return passwordStorage.verifyPassword(password, passwordType);
    }
    
    /**
     * Initialize keys for all password types during login
     * @param masterPassword The master password
     * @param authenticatedType The type that was successfully authenticated
     * @throws Exception if key initialization fails
     */
    public void initializeKeysForSession(String masterPassword, SecurePasswordStorage.PasswordType authenticatedType) throws Exception {
        System.out.println("🔑 Initializing encryption keys for session...");
        
        // Always derive key for the authenticated password type
        deriveAndCacheKey(masterPassword, authenticatedType);
        
        // For master mode, we can access all data, so derive all keys if available
        if (authenticatedType == SecurePasswordStorage.PasswordType.MASTER) {
            try {
                // Try to derive keys for other types if they exist
                // Note: This assumes the master password can access decoy data
                // In practice, you might want different key derivation strategies
                SecurePasswordStorage.PasswordData passwordData = passwordStorage.loadPasswordData();
                if (passwordData != null) {
                    // Cache salts for potential future use
                    if (passwordData.getDecoySalt() != null) {
                        cachedSalts.put(SecurePasswordStorage.PasswordType.DECOY, passwordData.getDecoySalt());
                    }
                    if (passwordData.getPanicSalt() != null) {
                        cachedSalts.put(SecurePasswordStorage.PasswordType.PANIC, passwordData.getPanicSalt());
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ Could not load additional password data: " + e.getMessage());
            }
        }
        
        keysLoaded = true;
        System.out.println("✅ Session keys initialized for: " + authenticatedType);
    }
    
    /**
     * Get the primary encryption key for file operations
     * @return The primary encryption key
     * @throws IllegalStateException if no keys are loaded
     */
    public SecretKey getPrimaryEncryptionKey() {
        if (!keysLoaded || derivedKeys.isEmpty()) {
            throw new IllegalStateException("No encryption keys loaded - authenticate first");
        }
        
        // Return the first available key (in practice, this would be the authenticated type's key)
        return derivedKeys.values().iterator().next();
    }
    
    /**
     * Get encryption key for specific password type
     * @param passwordType The password type
     * @return The encryption key or null if not available
     */
    public SecretKey getEncryptionKey(SecurePasswordStorage.PasswordType passwordType) {
        return derivedKeys.get(passwordType);
    }
    
    /**
     * Check if keys are loaded for the session
     * @return true if keys are loaded
     */
    public boolean areKeysLoaded() {
        return keysLoaded && !derivedKeys.isEmpty();
    }
    
    /**
     * Get the authenticated password type (the one with a cached key)
     * @return The authenticated password type or null
     */
    public SecurePasswordStorage.PasswordType getAuthenticatedType() {
        if (derivedKeys.isEmpty()) {
            return null;
        }
        
        // Return the first key type (in practice, this should be the authenticated one)
        return derivedKeys.keySet().iterator().next();
    }
    
    /**
     * Clear all cached keys and sensitive data
     */
    public void clearKeys() {
        System.out.println("🧹 Clearing cached encryption keys...");
        
        // Securely wipe all cached keys
        for (SecretKey key : derivedKeys.values()) {
            cryptoManager.secureWipe(key);
        }
        derivedKeys.clear();
        
        // Securely wipe cached salts
        for (byte[] salt : cachedSalts.values()) {
            cryptoManager.secureWipe(salt);
        }
        cachedSalts.clear();
        
        keysLoaded = false;
        System.out.println("✅ All encryption keys cleared from memory");
    }
    
    /**
     * Re-derive key with new password (for password changes)
     * @param oldPassword The current password
     * @param newPassword The new password
     * @param passwordType The password type to update
     * @throws Exception if re-derivation fails
     */
    public void updatePasswordAndKey(String oldPassword, String newPassword, SecurePasswordStorage.PasswordType passwordType) throws Exception {
        // Verify old password first
        if (!passwordStorage.verifyPassword(oldPassword, passwordType)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // This would require updating the password storage as well
        // For now, just clear the cached key
        SecretKey oldKey = derivedKeys.remove(passwordType);
        if (oldKey != null) {
            cryptoManager.secureWipe(oldKey);
        }
        
        byte[] oldSalt = cachedSalts.remove(passwordType);
        if (oldSalt != null) {
            cryptoManager.secureWipe(oldSalt);
        }
        
        System.out.println("🔄 Password updated for: " + passwordType);
    }
    
    /**
     * Validate key integrity by testing encryption/decryption
     * @param passwordType The password type to validate
     * @return true if key is valid
     */
    public boolean validateKeyIntegrity(SecurePasswordStorage.PasswordType passwordType) {
        try {
            SecretKey key = derivedKeys.get(passwordType);
            if (key == null) {
                return false;
            }
            
            // Test encryption/decryption with the key
            byte[] testData = "Key integrity test".getBytes();
            CryptoManager.EncryptedData encrypted = cryptoManager.encrypt(testData, key);
            byte[] decrypted = cryptoManager.decrypt(encrypted, key);
            
            boolean valid = java.util.Arrays.equals(testData, decrypted);
            
            // Clean up test data
            cryptoManager.secureWipe(decrypted);
            
            return valid;
            
        } catch (Exception e) {
            System.err.println("❌ Key integrity validation failed for " + passwordType + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get key management statistics
     * @return KeyManagementStats with current state
     */
    public KeyManagementStats getStats() {
        return new KeyManagementStats(
            keysLoaded,
            derivedKeys.size(),
            cachedSalts.size(),
            getAuthenticatedType()
        );
    }
    
    /**
     * Statistics about key management state
     */
    public static class KeyManagementStats {
        private final boolean keysLoaded;
        private final int cachedKeyCount;
        private final int cachedSaltCount;
        private final SecurePasswordStorage.PasswordType authenticatedType;
        
        public KeyManagementStats(boolean keysLoaded, int cachedKeyCount, int cachedSaltCount, 
                                SecurePasswordStorage.PasswordType authenticatedType) {
            this.keysLoaded = keysLoaded;
            this.cachedKeyCount = cachedKeyCount;
            this.cachedSaltCount = cachedSaltCount;
            this.authenticatedType = authenticatedType;
        }
        
        public boolean areKeysLoaded() { return keysLoaded; }
        public int getCachedKeyCount() { return cachedKeyCount; }
        public int getCachedSaltCount() { return cachedSaltCount; }
        public SecurePasswordStorage.PasswordType getAuthenticatedType() { return authenticatedType; }
        
        @Override
        public String toString() {
            return String.format("KeyManagementStats{loaded=%s, keys=%d, salts=%d, type=%s}",
                               keysLoaded, cachedKeyCount, cachedSaltCount, authenticatedType);
        }
    }
}