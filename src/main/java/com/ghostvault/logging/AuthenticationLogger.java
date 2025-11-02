package com.ghostvault.logging;

import com.ghostvault.security.VaultMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Specialized logger for authentication events and security audit
 */
public class AuthenticationLogger {
    
    private static final String LOG_FILE = System.getProperty("user.home") + "/.ghostvault/logs/auth.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Log authentication attempt
     */
    public static void logAuthenticationAttempt(String passwordLength, boolean success, VaultMode mode, String errorMessage) {
        String logEntry = String.format("[%s] AUTH_ATTEMPT: password_length=%s, success=%s, mode=%s, error=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            passwordLength,
            success,
            mode != null ? mode.toString() : "NONE",
            errorMessage != null ? errorMessage : "none"
        );
        
        writeLogEntry(logEntry);
        System.out.println("🔐 " + logEntry);
    }
    
    /**
     * Log failed authentication with remaining attempts
     */
    public static void logFailedAuthentication(int remainingAttempts, String reason) {
        String logEntry = String.format("[%s] AUTH_FAILED: remaining_attempts=%d, reason=%s",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            remainingAttempts,
            reason
        );
        
        writeLogEntry(logEntry);
        System.out.println("❌ " + logEntry);
    }
    
    /**
     * Log system lockout event
     */
    public static void logSystemLockout(long lockoutDurationSeconds) {
        String logEntry = String.format("[%s] SYSTEM_LOCKOUT: duration_seconds=%d, reason=too_many_failed_attempts",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            lockoutDurationSeconds
        );
        
        writeLogEntry(logEntry);
        System.out.println("🔒 " + logEntry);
    }
    
    /**
     * Log panic mode activation
     */
    public static void logPanicModeActivation() {
        String logEntry = String.format("[%s] PANIC_MODE: status=activated, action=system_wipe_initiated",
            LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
        
        writeLogEntry(logEntry);
        System.out.println("🚨 " + logEntry);
    }
    
    /**
     * Log successful vault mode access
     */
    public static void logVaultAccess(VaultMode mode) {
        String logEntry = String.format("[%s] VAULT_ACCESS: mode=%s, status=granted",
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            mode.toString()
        );
        
        writeLogEntry(logEntry);
        System.out.println("✅ " + logEntry);
    }
    
    /**
     * Write log entry to file
     */
    private static void writeLogEntry(String logEntry) {
        try {
            // Ensure log directory exists
            Files.createDirectories(Paths.get(LOG_FILE).getParent());
            
            // Append to log file
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                writer.write(logEntry + System.lineSeparator());
                writer.flush();
            }
        } catch (IOException e) {
            System.err.println("⚠️ Failed to write authentication log: " + e.getMessage());
        }
    }
}