package com.ghostvault.security;

import com.ghostvault.logging.AuthenticationLogger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Secure authentication manager that properly validates passwords and determines vault modes
 */
public class SecureAuthenticationManager {
    
    // Password storage file
    private static final String PASSWORDS_FILE = System.getProperty("user.home") + "/.ghostvault/passwords.dat";
    
    // User-defined passwords (loaded from secure storage)
    private String masterPassword = null;
    private String decoyPassword = null;
    private String panicPassword = null;
    
    // Security settings
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 300000; // 5 minutes
    
    // Attempt tracking - persistent across application restarts
    private final AtomicInteger failedAttempts = new AtomicInteger(0);
    private final AtomicLong lockoutEndTime = new AtomicLong(0);
    private static final String ATTEMPTS_FILE = System.getProperty("user.home") + "/.ghostvault/auth_attempts.dat";
    
    /**
     * Constructor - load persistent attempt data and user passwords
     */
    public SecureAuthenticationManager() {
        System.out.println("🔐 Initializing SecureAuthenticationManager...");
        loadAttemptData();
        loadUserPasswords();
        System.out.println("🔍 Password status: master=" + (masterPassword != null) + 
                          ", decoy=" + (decoyPassword != null) + 
                          ", panic=" + (panicPassword != null));
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
     * Check if password is valid master password
     */
    public boolean isValidMasterPassword(String password) {
        boolean isValid = masterPassword != null && masterPassword.equals(password);
        System.out.println("🔍 Master password check: input='" + password + "' stored='" + masterPassword + "' valid=" + isValid);
        return isValid;
    }
    
    /**
     * Check if password is valid decoy password
     */
    public boolean isValidDecoyPassword(String password) {
        boolean isValid = decoyPassword != null && decoyPassword.equals(password);
        System.out.println("🔍 Decoy password check: input='" + password + "' stored='" + decoyPassword + "' valid=" + isValid);
        return isValid;
    }
    
    /**
     * Check if password is panic password
     */
    public boolean isPanicPassword(String password) {
        boolean isValid = panicPassword != null && panicPassword.equals(password);
        System.out.println("🔍 Panic password check: input='" + password + "' stored='" + panicPassword + "' valid=" + isValid);
        return isValid;
    }
    
    /**
     * Check if setup is complete (user has set custom passwords)
     */
    public boolean isSetupComplete() {
        return masterPassword != null && decoyPassword != null && panicPassword != null;
    }
    
    /**
     * Set user passwords during setup
     */
    public void setUserPasswords(String master, String decoy, String panic) {
        System.out.println("🔐 Setting user passwords: master=" + (master != null && !master.isEmpty()) + 
                          ", decoy=" + (decoy != null && !decoy.isEmpty()) + 
                          ", panic=" + (panic != null && !panic.isEmpty()));
        this.masterPassword = master;
        this.decoyPassword = decoy;
        this.panicPassword = panic;
        saveUserPasswords();
        System.out.println("✅ User passwords set and saved securely");
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
     * Load user passwords from secure storage
     */
    private void loadUserPasswords() {
        try {
            java.nio.file.Path passwordsPath = java.nio.file.Paths.get(PASSWORDS_FILE);
            if (java.nio.file.Files.exists(passwordsPath)) {
                String data = new String(java.nio.file.Files.readAllBytes(passwordsPath));
                String[] parts = data.split("\n");
                if (parts.length == 3) {
                    masterPassword = parts[0].trim();
                    decoyPassword = parts[1].trim();
                    panicPassword = parts[2].trim();
                    System.out.println("🔐 Loaded user passwords from secure storage");
                    System.out.println("🔍 Debug - Master: '" + masterPassword + "' (length: " + masterPassword.length() + ")");
                    System.out.println("🔍 Debug - Decoy: '" + decoyPassword + "' (length: " + decoyPassword.length() + ")");
                    System.out.println("🔍 Debug - Panic: '" + panicPassword + "' (length: " + panicPassword.length() + ")");
                }
            } else {
                System.out.println("📋 No user passwords found - setup required");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load user passwords: " + e.getMessage());
        }
    }
    
    /**
     * Save user passwords to secure storage
     */
    private void saveUserPasswords() {
        try {
            java.nio.file.Path passwordsPath = java.nio.file.Paths.get(PASSWORDS_FILE);
            System.out.println("💾 Saving passwords to: " + passwordsPath.toString());
            java.nio.file.Files.createDirectories(passwordsPath.getParent());
            
            String data = masterPassword + "\n" + decoyPassword + "\n" + panicPassword;
            java.nio.file.Files.write(passwordsPath, data.getBytes());
            
            // Verify the file was created
            if (java.nio.file.Files.exists(passwordsPath)) {
                long fileSize = java.nio.file.Files.size(passwordsPath);
                System.out.println("💾 User passwords saved to secure storage (" + fileSize + " bytes)");
            } else {
                System.err.println("❌ Password file was not created!");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to save user passwords: " + e.getMessage());
            e.printStackTrace();
        }
    }
}