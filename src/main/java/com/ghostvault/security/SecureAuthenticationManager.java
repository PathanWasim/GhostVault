package com.ghostvault.security;

import com.ghostvault.logging.AuthenticationLogger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Secure authentication manager with encrypted password storage
 * Uses PBKDF2 hashing instead of plain text password storage
 */
public class SecureAuthenticationManager {
    
    // Secure password storage
    private final SecurePasswordStorage passwordStorage;
    private final PasswordMigrationUtility migrationUtility;
    
    // Migration flag
    private boolean migrationChecked = false;
    
    // Security settings
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 300000; // 5 minutes
    
    // Attempt tracking - persistent across application restarts
    private final AtomicInteger failedAttempts = new AtomicInteger(0);
    private final AtomicLong lockoutEndTime = new AtomicLong(0);
    private static final String ATTEMPTS_FILE = System.getProperty("user.home") + "/.ghostvault/auth_attempts.dat";
    
    /**
     * Constructor - initialize secure password storage and load attempt data
     */
    public SecureAuthenticationManager() {
        System.out.println("🔐 Initializing SecureAuthenticationManager with encrypted storage...");
        this.passwordStorage = new SecurePasswordStorage();
        this.migrationUtility = new PasswordMigrationUtility();
        loadAttemptData();
        checkAndPerformMigration();
        System.out.println("🔍 Password storage status: secure=" + passwordStorage.isUsingSecureStorage());
    }
    
    /**
     * Authenticate user with password and return result
     */
    public AuthenticationResult authenticate(String password) {
        System.out.println("🔐 SecureAuthenticationManager: Authenticating password [" + password.length() + " chars]");
        
        // Check if system is locked
        if (isSystemLocked()) {
            long remainingTime = (lockoutEndTime.get() - System.currentTimeMillis()) / 1000;
            System.out.println("❌ System locked for " + remainingTime + " more seconds");
            return new AuthenticationResult(false, null, 
                "System locked. Try again in " + remainingTime + " seconds.", false, 0);
        }
        
        // Validate password
        if (password == null || password.trim().isEmpty()) {
            incrementFailedAttempts();
            return new AuthenticationResult(false, null, "Password cannot be empty", false, 
                MAX_FAILED_ATTEMPTS - failedAttempts.get());
        }
        
        // Check for panic password first (highest priority)
        if (isPanicPassword(password)) {
            System.out.println("🚨 PANIC PASSWORD DETECTED - Initiating system wipe");
            AuthenticationLogger.logPanicModeActivation();
            AuthenticationLogger.logAuthenticationAttempt(String.valueOf(password.length()), true, VaultMode.PANIC, null);
            return new AuthenticationResult(true, VaultMode.PANIC, "Panic mode activated", true, 0);
        }
        
        // Check master password
        if (isValidMasterPassword(password)) {
            System.out.println("✅ Master password validated");
            resetFailedAttempts();
            AuthenticationLogger.logAuthenticationAttempt(String.valueOf(password.length()), true, VaultMode.MASTER, null);
            AuthenticationLogger.logVaultAccess(VaultMode.MASTER);
            return new AuthenticationResult(true, VaultMode.MASTER, "Master mode access granted", false, 0);
        }
        
        // Check decoy password
        if (isValidDecoyPassword(password)) {
            System.out.println("✅ Decoy password validated");
            resetFailedAttempts();
            AuthenticationLogger.logAuthenticationAttempt(String.valueOf(password.length()), true, VaultMode.DECOY, null);
            AuthenticationLogger.logVaultAccess(VaultMode.DECOY);
            return new AuthenticationResult(true, VaultMode.DECOY, "Decoy mode access granted", false, 0);
        }
        
        // Invalid password
        incrementFailedAttempts();
        int remaining = MAX_FAILED_ATTEMPTS - failedAttempts.get();
        System.out.println("❌ Invalid password. Attempts remaining: " + remaining);
        
        String errorMessage = "Invalid password. " + remaining + " attempts remaining.";
        AuthenticationLogger.logAuthenticationAttempt(String.valueOf(password.length()), false, null, errorMessage);
        AuthenticationLogger.logFailedAuthentication(remaining, "invalid_password");
        
        if (remaining <= 0) {
            lockoutEndTime.set(System.currentTimeMillis() + LOCKOUT_DURATION_MS);
            saveAttemptData(); // Persist the lockout
            System.out.println("🔒 System locked due to too many failed attempts");
            AuthenticationLogger.logSystemLockout(LOCKOUT_DURATION_MS / 1000);
            return new AuthenticationResult(false, null, "Too many failed attempts. System locked for 5 minutes.", false, 0);
        }
        
        return new AuthenticationResult(false, null, errorMessage, false, remaining);
    }
    
    /**
     * Determine vault mode based on password (used by legacy code)
     */
    public VaultMode determineVaultMode(String password) {
        AuthenticationResult result = authenticate(password);
        return result.getMode();
    }
    
    /**
     * Check if password is valid master password using secure hash verification
     */
    public boolean isValidMasterPassword(String password) {
        try {
            boolean isValid = passwordStorage.verifyPassword(password, SecurePasswordStorage.PasswordType.MASTER);
            System.out.println("🔍 Master password check: valid=" + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("❌ Master password verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if password is valid decoy password using secure hash verification
     */
    public boolean isValidDecoyPassword(String password) {
        try {
            boolean isValid = passwordStorage.verifyPassword(password, SecurePasswordStorage.PasswordType.DECOY);
            System.out.println("🔍 Decoy password check: valid=" + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("❌ Decoy password verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if password is panic password using secure hash verification
     */
    public boolean isPanicPassword(String password) {
        try {
            boolean isValid = passwordStorage.verifyPassword(password, SecurePasswordStorage.PasswordType.PANIC);
            System.out.println("🔍 Panic password check: valid=" + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("❌ Panic password verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if setup is complete (user has set custom passwords)
     */
    public boolean isSetupComplete() {
        try {
            SecurePasswordStorage.PasswordData passwordData = passwordStorage.loadPasswordData();
            boolean complete = passwordData != null && 
                             passwordData.getMasterHash() != null && 
                             passwordData.getDecoyHash() != null && 
                             passwordData.getPanicHash() != null;
            System.out.println("🔍 Setup complete check: " + complete);
            return complete;
        } catch (Exception e) {
            System.err.println("❌ Setup check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Set user passwords during setup using secure hash storage
     */
    public void setUserPasswords(String master, String decoy, String panic) {
        System.out.println("🔐 Setting user passwords with secure hashing...");
        try {
            passwordStorage.storePasswordHashes(master, decoy, panic);
            System.out.println("✅ User passwords hashed and saved securely");
        } catch (Exception e) {
            System.err.println("❌ Failed to save user passwords: " + e.getMessage());
            throw new RuntimeException("Failed to save passwords securely", e);
        }
    }
    
    /**
     * Increment failed attempt counter
     */
    public void incrementFailedAttempts() {
        int attempts = failedAttempts.incrementAndGet();
        saveAttemptData(); // Persist the change
        System.out.println("⚠️ Failed attempts: " + attempts + "/" + MAX_FAILED_ATTEMPTS);
    }
    
    /**
     * Reset failed attempts counter
     */
    private void resetFailedAttempts() {
        failedAttempts.set(0);
        lockoutEndTime.set(0);
        saveAttemptData(); // Persist the reset
        System.out.println("✅ Failed attempts counter reset");
    }
    
    /**
     * Check if system is currently locked
     */
    public boolean isSystemLocked() {
        long currentTime = System.currentTimeMillis();
        long lockoutEnd = lockoutEndTime.get();
        
        if (lockoutEnd > 0 && currentTime < lockoutEnd) {
            return true;
        }
        
        // Clear lockout if time has passed
        if (lockoutEnd > 0 && currentTime >= lockoutEnd) {
            lockoutEndTime.set(0);
            failedAttempts.set(0);
            System.out.println("🔓 System lockout expired, access restored");
        }
        
        return false;
    }
    
    /**
     * Get remaining failed attempts before lockout
     */
    public int getRemainingAttempts() {
        return Math.max(0, MAX_FAILED_ATTEMPTS - failedAttempts.get());
    }
    
    /**
     * Get current failed attempts count
     */
    public int getFailedAttempts() {
        return failedAttempts.get();
    }
    
    /**
     * Load persistent attempt data from disk
     */
    private void loadAttemptData() {
        try {
            java.nio.file.Path attemptsPath = java.nio.file.Paths.get(ATTEMPTS_FILE);
            if (java.nio.file.Files.exists(attemptsPath)) {
                String data = new String(java.nio.file.Files.readAllBytes(attemptsPath));
                String[] parts = data.split(",");
                if (parts.length == 2) {
                    failedAttempts.set(Integer.parseInt(parts[0]));
                    lockoutEndTime.set(Long.parseLong(parts[1]));
                    System.out.println("🔐 Loaded persistent auth data: " + failedAttempts.get() + " attempts, lockout until " + lockoutEndTime.get());
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load attempt data: " + e.getMessage());
        }
    }
    
    /**
     * Save persistent attempt data to disk
     */
    private void saveAttemptData() {
        try {
            java.nio.file.Path attemptsPath = java.nio.file.Paths.get(ATTEMPTS_FILE);
            java.nio.file.Files.createDirectories(attemptsPath.getParent());
            String data = failedAttempts.get() + "," + lockoutEndTime.get();
            java.nio.file.Files.write(attemptsPath, data.getBytes());
        } catch (Exception e) {
            System.err.println("⚠️ Failed to save attempt data: " + e.getMessage());
        }
    }
    
    /**
     * Check for and perform migration from plain text passwords if needed
     */
    private void checkAndPerformMigration() {
        if (migrationChecked) {
            return;
        }
        
        migrationChecked = true;
        
        if (migrationUtility.isMigrationNeeded()) {
            System.out.println("🔄 Plain text passwords detected - performing automatic migration...");
            PasswordMigrationUtility.MigrationResult result = migrationUtility.performMigration();
            
            if (result.isSuccess()) {
                System.out.println("✅ Password migration completed successfully");
            } else {
                System.err.println("❌ Password migration failed: " + result.getMessage());
                // Continue with plain text for now, but warn user
                System.err.println("⚠️ WARNING: Passwords are still stored in plain text!");
            }
        }
    }
    
    /**
     * Get salt for key derivation (used by file encryption)
     * @param type The password type
     * @return The salt for key derivation
     */
    public byte[] getSaltForKeyDerivation(SecurePasswordStorage.PasswordType type) {
        try {
            return passwordStorage.getSaltForKeyDerivation(type);
        } catch (Exception e) {
            System.err.println("❌ Failed to get salt for key derivation: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if using secure password storage
     * @return true if passwords are stored securely
     */
    public boolean isUsingSecureStorage() {
        return passwordStorage.isUsingSecureStorage();
    }
}