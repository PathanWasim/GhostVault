package com.ghostvault.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility for migrating plain text passwords to secure encrypted storage
 */
public class PasswordMigrationUtility {
    
    private final SecurePasswordStorage secureStorage;
    private final Path vaultDirectory;
    private final Path plainPasswordFile;
    private final Path backupDirectory;
    
    public PasswordMigrationUtility() {
        this(System.getProperty("user.home") + "/.ghostvault");
    }
    
    public PasswordMigrationUtility(String vaultPath) {
        this.vaultDirectory = Paths.get(vaultPath);
        this.secureStorage = new SecurePasswordStorage(vaultPath);
        this.plainPasswordFile = vaultDirectory.resolve(SecurityConfiguration.PLAIN_PASSWORD_FILE);
        this.backupDirectory = vaultDirectory.resolve("backups");
    }
    
    /**
     * Check if migration is needed
     * @return true if plain text passwords exist and need migration
     */
    public boolean isMigrationNeeded() {
        return Files.exists(plainPasswordFile) && !secureStorage.isUsingSecureStorage();
    }
    
    /**
     * Perform migration from plain text to encrypted password storage
     * @return MigrationResult containing success status and details
     */
    public MigrationResult performMigration() {
        try {
            System.out.println("🔄 Starting password migration from plain text to encrypted storage...");
            
            // Step 1: Validate preconditions
            if (!Files.exists(plainPasswordFile)) {
                return new MigrationResult(false, "No plain text password file found", null);
            }
            
            if (secureStorage.isUsingSecureStorage()) {
                return new MigrationResult(false, "Already using secure storage", null);
            }
            
            // Step 2: Create backup
            String backupPath = createBackup();
            System.out.println("📦 Created backup: " + backupPath);
            
            // Step 3: Read plain text passwords
            PlainTextPasswords passwords = readPlainTextPasswords();
            if (passwords == null) {
                return new MigrationResult(false, "Failed to read plain text passwords", backupPath);
            }
            
            // Step 4: Validate passwords
            if (!validatePasswords(passwords)) {
                return new MigrationResult(false, "Invalid password format in plain text file", backupPath);
            }
            
            // Step 5: Store as encrypted hashes
            secureStorage.storePasswordHashes(
                passwords.masterPassword,
                passwords.decoyPassword,
                passwords.panicPassword
            );
            
            // Step 6: Verify migration success
            if (!verifyMigration(passwords)) {
                // Rollback on verification failure
                rollbackMigration(backupPath);
                return new MigrationResult(false, "Migration verification failed - rolled back", backupPath);
            }
            
            // Step 7: Clean up plain text file (already done by SecurePasswordStorage)
            System.out.println("✅ Password migration completed successfully");
            
            return new MigrationResult(true, "Migration completed successfully", backupPath);
            
        } catch (Exception e) {
            System.err.println("❌ Migration failed: " + e.getMessage());
            e.printStackTrace();
            return new MigrationResult(false, "Migration failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Rollback migration by restoring from backup
     * @param backupPath The backup file path
     * @return true if rollback successful
     */
    public boolean rollbackMigration(String backupPath) {
        try {
            if (backupPath == null || !Files.exists(Paths.get(backupPath))) {
                System.err.println("❌ Cannot rollback: backup file not found");
                return false;
            }
            
            System.out.println("🔄 Rolling back migration...");
            
            // Restore plain text password file
            Files.copy(Paths.get(backupPath), plainPasswordFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Remove encrypted password file
            Path encryptedFile = vaultDirectory.resolve(SecurityConfiguration.ENCRYPTED_PASSWORD_FILE);
            if (Files.exists(encryptedFile)) {
                Files.delete(encryptedFile);
            }
            
            System.out.println("✅ Migration rollback completed");
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Rollback failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create a backup of the plain text password file
     * @return backup file path
     * @throws IOException if backup creation fails
     */
    private String createBackup() throws IOException {
        Files.createDirectories(backupDirectory);
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupFileName = "passwords_backup_" + timestamp + ".dat";
        Path backupFile = backupDirectory.resolve(backupFileName);
        
        Files.copy(plainPasswordFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        
        return backupFile.toString();
    }
    
    /**
     * Read passwords from plain text file
     * @return PlainTextPasswords or null if reading fails
     */
    private PlainTextPasswords readPlainTextPasswords() {
        try {
            String content = new String(Files.readAllBytes(plainPasswordFile));
            String[] lines = content.split("\n");
            
            if (lines.length >= 3) {
                return new PlainTextPasswords(
                    lines[0].trim(),
                    lines[1].trim(),
                    lines[2].trim()
                );
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("❌ Failed to read plain text passwords: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate that passwords meet basic requirements
     * @param passwords The passwords to validate
     * @return true if valid
     */
    private boolean validatePasswords(PlainTextPasswords passwords) {
        if (passwords == null) {
            return false;
        }
        
        // Check that all passwords are present and non-empty
        if (passwords.masterPassword == null || passwords.masterPassword.trim().isEmpty() ||
            passwords.decoyPassword == null || passwords.decoyPassword.trim().isEmpty() ||
            passwords.panicPassword == null || passwords.panicPassword.trim().isEmpty()) {
            return false;
        }
        
        // Check that all passwords are different
        if (passwords.masterPassword.equals(passwords.decoyPassword) ||
            passwords.masterPassword.equals(passwords.panicPassword) ||
            passwords.decoyPassword.equals(passwords.panicPassword)) {
            System.err.println("❌ All passwords must be different");
            return false;
        }
        
        return true;
    }
    
    /**
     * Verify that migration was successful by testing password verification
     * @param originalPasswords The original plain text passwords
     * @return true if verification successful
     */
    private boolean verifyMigration(PlainTextPasswords originalPasswords) {
        try {
            // Test each password type
            boolean masterValid = secureStorage.verifyPassword(originalPasswords.masterPassword, SecurePasswordStorage.PasswordType.MASTER);
            boolean decoyValid = secureStorage.verifyPassword(originalPasswords.decoyPassword, SecurePasswordStorage.PasswordType.DECOY);
            boolean panicValid = secureStorage.verifyPassword(originalPasswords.panicPassword, SecurePasswordStorage.PasswordType.PANIC);
            
            // Test that wrong passwords fail
            boolean wrongPasswordFails = !secureStorage.verifyPassword("WrongPassword123!", SecurePasswordStorage.PasswordType.MASTER);
            
            boolean allValid = masterValid && decoyValid && panicValid && wrongPasswordFails;
            
            if (allValid) {
                System.out.println("✅ Migration verification successful");
            } else {
                System.err.println("❌ Migration verification failed - passwords don't match");
            }
            
            return allValid;
            
        } catch (Exception e) {
            System.err.println("❌ Migration verification failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Container for plain text passwords
     */
    private static class PlainTextPasswords {
        final String masterPassword;
        final String decoyPassword;
        final String panicPassword;
        
        PlainTextPasswords(String master, String decoy, String panic) {
            this.masterPassword = master;
            this.decoyPassword = decoy;
            this.panicPassword = panic;
        }
    }
    
    /**
     * Result of migration operation
     */
    public static class MigrationResult {
        private final boolean success;
        private final String message;
        private final String backupPath;
        
        public MigrationResult(boolean success, String message, String backupPath) {
            this.success = success;
            this.message = message;
            this.backupPath = backupPath;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBackupPath() { return backupPath; }
        
        @Override
        public String toString() {
            return String.format("MigrationResult{success=%s, message='%s', backup='%s'}", 
                               success, message, backupPath);
        }
    }
}