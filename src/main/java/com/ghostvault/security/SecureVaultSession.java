package com.ghostvault.security;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import javax.crypto.SecretKey;

/**
 * Secure vault session that manages authentication and key lifecycle
 * Integrates password authentication with key derivation and secure cleanup
 */
public class SecureVaultSession {
    
    private final SecureAuthenticationManager authManager;
    private final KeyManagementService keyManager;
    private final SecureMemoryManager memoryManager;
    
    // Session state
    private boolean authenticated = false;
    private SecurePasswordStorage.PasswordType authenticatedType = null;
    private VaultMode currentMode = null;
    
    // Vault components
    private FileManager fileManager;
    private MetadataManager metadataManager;
    
    public SecureVaultSession() {
        this.authManager = new SecureAuthenticationManager();
        this.keyManager = new KeyManagementService();
        this.memoryManager = SecureMemoryManager.getInstance();
    }
    
    public SecureVaultSession(String vaultPath) {
        this.authManager = new SecureAuthenticationManager();
        this.keyManager = new KeyManagementService(vaultPath);
        this.memoryManager = SecureMemoryManager.getInstance();
        
        // Initialize vault components with path
        this.fileManager = new FileManager(vaultPath);
        this.metadataManager = new MetadataManager(vaultPath + "/metadata/metadata.json");
    }
    
    /**
     * Authenticate user and initialize secure session
     * @param password The user's password
     * @return AuthenticationResult with session details
     */
    public AuthenticationResult authenticate(String password) {
        try {
            System.out.println("🔐 Starting secure authentication...");
            
            // Perform authentication
            AuthenticationResult authResult = authManager.authenticate(password);
            
            if (authResult.isSuccess()) {
                // Determine password type and initialize keys
                SecurePasswordStorage.PasswordType passwordType = determinePasswordType(password);
                
                if (passwordType != null) {
                    // Initialize encryption keys for the session
                    keyManager.initializeKeysForSession(password, passwordType);
                    
                    // Configure vault components with password
                    configureVaultComponents(password);
                    
                    // Update session state
                    authenticated = true;
                    authenticatedType = passwordType;
                    currentMode = authResult.getMode();
                    
                    System.out.println("✅ Secure session initialized for: " + passwordType);
                } else {
                    System.err.println("❌ Could not determine password type");
                    return new AuthenticationResult(false, null, "Authentication failed", false, 0);
                }
            }
            
            return authResult;
            
        } catch (Exception e) {
            System.err.println("❌ Authentication failed: " + e.getMessage());
            return new AuthenticationResult(false, null, "Authentication error: " + e.getMessage(), false, 0);
        }
    }
    
    /**
     * Logout and clear all sensitive session data
     */
    public void logout() {
        System.out.println("🔓 Logging out and clearing session data...");
        
        // Clear vault component passwords
        if (fileManager != null) {
            fileManager.clearSensitiveData();
        }
        if (metadataManager != null) {
            metadataManager.clearSensitiveData();
        }
        
        // Clear encryption keys
        keyManager.clearKeys();
        
        // Emergency memory cleanup
        memoryManager.cleanupAllTrackedData();
        
        // Reset session state
        authenticated = false;
        authenticatedType = null;
        currentMode = null;
        
        System.out.println("✅ Secure logout completed");
    }
    
    /**
     * Panic mode - immediately destroy all sensitive data
     */
    public void panicMode() {
        System.out.println("🚨 PANIC MODE ACTIVATED - DESTROYING ALL SENSITIVE DATA");
        
        // Emergency cleanup of all components
        if (fileManager != null) {
            fileManager.clearSensitiveData();
        }
        if (metadataManager != null) {
            metadataManager.clearSensitiveData();
        }
        
        // Emergency key cleanup
        keyManager.clearKeys();
        
        // Emergency memory cleanup
        memoryManager.emergencyCleanup();
        
        // Reset session state
        authenticated = false;
        authenticatedType = null;
        currentMode = VaultMode.PANIC;
        
        System.out.println("🚨 PANIC MODE COMPLETE - ALL SENSITIVE DATA DESTROYED");
    }
    
    /**
     * Get the current vault mode
     * @return The current vault mode or null if not authenticated
     */
    public VaultMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Check if session is authenticated
     * @return true if authenticated
     */
    public boolean isAuthenticated() {
        return authenticated && keyManager.areKeysLoaded();
    }
    
    /**
     * Get the authenticated password type
     * @return The password type or null if not authenticated
     */
    public SecurePasswordStorage.PasswordType getAuthenticatedType() {
        return authenticatedType;
    }
    
    /**
     * Get the file manager for this session
     * @return FileManager instance
     * @throws IllegalStateException if not authenticated
     */
    public FileManager getFileManager() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Session not authenticated");
        }
        return fileManager;
    }
    
    /**
     * Get the metadata manager for this session
     * @return MetadataManager instance
     * @throws IllegalStateException if not authenticated
     */
    public MetadataManager getMetadataManager() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Session not authenticated");
        }
        return metadataManager;
    }
    
    /**
     * Get the primary encryption key for the session
     * @return The encryption key
     * @throws IllegalStateException if not authenticated
     */
    public SecretKey getPrimaryEncryptionKey() {
        if (!isAuthenticated()) {
            throw new IllegalStateException("Session not authenticated");
        }
        return keyManager.getPrimaryEncryptionKey();
    }
    
    /**
     * Validate session integrity
     * @return true if session is valid and secure
     */
    public boolean validateSessionIntegrity() {
        if (!authenticated || authenticatedType == null) {
            return false;
        }
        
        // Validate key integrity
        return keyManager.validateKeyIntegrity(authenticatedType);
    }
    
    /**
     * Get session statistics
     * @return SessionStats with current state
     */
    public SessionStats getSessionStats() {
        return new SessionStats(
            authenticated,
            authenticatedType,
            currentMode,
            keyManager.getStats(),
            memoryManager.getStats()
        );
    }
    
    /**
     * Determine password type by testing against stored hashes
     * @param password The password to test
     * @return The password type or null if not found
     */
    private SecurePasswordStorage.PasswordType determinePasswordType(String password) {
        try {
            // Test against each password type
            for (SecurePasswordStorage.PasswordType type : SecurePasswordStorage.PasswordType.values()) {
                if (keyManager.authenticatePassword(password, type)) {
                    return type;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error determining password type: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Configure vault components with the authenticated password
     * @param password The authenticated password
     */
    private void configureVaultComponents(String password) {
        if (fileManager == null) {
            fileManager = new FileManager();
        }
        if (metadataManager == null) {
            metadataManager = new MetadataManager();
        }
        
        // Set passwords for encryption operations
        fileManager.setPassword(password);
        fileManager.setEncryptionEnabled(true);
        
        metadataManager.setPassword(password);
        metadataManager.setEncryptionEnabled(true);
        
        System.out.println("🔧 Vault components configured with encryption");
    }
    
    /**
     * Session statistics
     */
    public static class SessionStats {
        private final boolean authenticated;
        private final SecurePasswordStorage.PasswordType authenticatedType;
        private final VaultMode currentMode;
        private final KeyManagementService.KeyManagementStats keyStats;
        private final SecureMemoryManager.MemoryStats memoryStats;
        
        public SessionStats(boolean authenticated, SecurePasswordStorage.PasswordType authenticatedType,
                          VaultMode currentMode, KeyManagementService.KeyManagementStats keyStats,
                          SecureMemoryManager.MemoryStats memoryStats) {
            this.authenticated = authenticated;
            this.authenticatedType = authenticatedType;
            this.currentMode = currentMode;
            this.keyStats = keyStats;
            this.memoryStats = memoryStats;
        }
        
        public boolean isAuthenticated() { return authenticated; }
        public SecurePasswordStorage.PasswordType getAuthenticatedType() { return authenticatedType; }
        public VaultMode getCurrentMode() { return currentMode; }
        public KeyManagementService.KeyManagementStats getKeyStats() { return keyStats; }
        public SecureMemoryManager.MemoryStats getMemoryStats() { return memoryStats; }
        
        @Override
        public String toString() {
            return String.format("SessionStats{auth=%s, type=%s, mode=%s, keys=%s, memory=%s}",
                               authenticated, authenticatedType, currentMode, keyStats, memoryStats);
        }
    }
}