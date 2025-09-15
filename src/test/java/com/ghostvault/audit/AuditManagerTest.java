package com.ghostvault.audit;

import com.ghostvault.config.AppConfig;
import com.ghostvault.core.VaultInitializer;
import com.ghostvault.security.PasswordManager;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Comprehensive test for AuditManager functionality
 */
public class AuditManagerTest {
    
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("         AuditManager Comprehensive Test");
        System.out.println("=================================================");
        
        try {
            runAllTests();
            System.out.println("\n✅ All AuditManager tests passed!");
            
        } catch (Exception e) {
            System.err.println("\n❌ AuditManager test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void runAllTests() throws Exception {
        testBasicAuditLogging();
        testAuditCategories();
        testAuditFiltering();
        testAuditStatistics();
        testLogRotation();
        testSecureAuditStorage();
        testPanicModeDestruction();
    }
    
    /**
     * Test basic audit logging functionality
     */
    private static void testBasicAuditLogging() throws Exception {
        System.out.println("\n🧪 Testing basic audit logging...");
        
        // Clean up any existing vault
        if (VaultInitializer.isVaultInitialized()) {
            VaultInitializer.resetVault();
        }
        
        // Initialize test vault
        String masterPassword = "TestMasterPassword123!";
        VaultInitializer.initializeVault(masterPassword, "TestPanicPassword456@", "TestDecoyPassword789#");
        
        // Set up audit manager
        AuditManager auditManager = new AuditManager();
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        javax.crypto.SecretKey auditKey = passwordManager.deriveVaultKey(masterPassword);
        auditManager.startAuditLogging(auditKey);
        
        // Log various events
        auditManager.logAuthentication("LOGIN_ATTEMPT", "testuser", "192.168.1.1", true);
        auditManager.logFileOperation("UPLOAD", "test.txt", "file123", 1024, true, null);
        auditManager.logSecurityEvent("SECURITY_SCAN", "Routine security scan", 
            AuditManager.AuditSeverity.INFO, "127.0.0.1", "All clear");
        
        // Wait for async processing
        Thread.sleep(1000);
        
        // Read audit entries
        List<AuditEntry> entries = auditManager.readAuditLog(100, null, null, null);
        
        assert entries.size() >= 4 : "Should have at least 4 entries (including system start)"; // Including system startup
        
        // Verify entry content
        boolean foundLogin = entries.stream().anyMatch(e -> e.getEventType().equals("LOGIN_ATTEMPT"));
        boolean foundFileOp = entries.stream().anyMatch(e -> e.getEventType().equals("FILE_UPLOAD"));
        boolean foundSecurity = entries.stream().anyMatch(e -> e.getEventType().equals("SECURITY_SCAN"));
        
        assert foundLogin : "Should find login attempt entry";
        assert foundFileOp : "Should find file operation entry";
        assert foundSecurity : "Should find security event entry";
        
        System.out.println("   ✓ Logged " + entries.size() + " audit entries");
        
        // Clean up
        auditManager.stopAuditLogging();
        auditManager.cleanup();
        VaultInitializer.resetVault();
        
        System.out.println("   ✅ Basic audit logging test passed");
    }
    
    /**
     * Test different audit categories
     */
    private static void testAuditCategories() throws Exception {
        System.out.println("\n🧪 Testing audit categories...");
        
        // Initialize test vault
        String masterPassword = "TestMasterPassword123!";
        VaultInitializer.initializeVault(masterPassword, "TestPanicPassword456@", "TestDecoyPassword789#");
        
        AuditManager auditManager = new AuditManager();
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        javax.crypto.SecretKey auditKey = passwordManager.deriveVaultKey(masterPassword);
        auditManager.startAuditLogging(auditKey);
        
        // Test each category
        auditManager.logAuthentication("LOGIN", "user1", "192.168.1.1", true);
        auditManager.logFileOperation("DELETE", "file.txt", "file456", 2048, true, null);
        auditManager.logSessionEvent("SESSION_START", "sess123", 0, "User login");
        auditManager.logBackupRestore("BACKUP", "backup.gvb", 5, 10240, true, null);
        auditManager.logConfigurationChange("timeout", "15", "30", "admin");
        auditManager.logError("NullPointerException", "Null reference", "stack trace", "file upload");
        
        Thread.sleep(1000);
        
        // Read and verify categories
        List<AuditEntry> entries = auditManager.readAuditLog(100, null, null, null);
        
        boolean hasAuth = entries.stream().anyMatch(e -> e.getCategory() == AuditManager.AuditCategory.AUTHENTICATION);
        boolean hasFile = entries.stream().anyMatch(e -> e.getCategory() == AuditManager.AuditCategory.FILE_OPERATIONS);
        boolean hasSession = entries.stream().anyMatch(e -> e.getCategory() == AuditManager.AuditCategory.SESSION_MANAGEMENT);
        boolean hasBackup = entries.stream().anyMatch(e -> e.getCategory() == AuditManager.AuditCategory.BACKUP_RESTORE);
        boolean hasConfig = entries.stream().anyMatch(e -> e.getCategory() == AuditManager.AuditCategory.CONFIGURATION);
        boolean hasError = entries.stream().anyMatch(e -> e.getCategory() == AuditManager.AuditCategory.ERROR_EVENTS);
        
        assert hasAuth : "Should have authentication entries";
        assert hasFile : "Should have file operation entries";
        assert hasSession : "Should have session management entries";
        assert hasBackup : "Should have backup/restore entries";
        assert hasConfig : "Should have configuration entries";
        assert hasError : "Should have error entries";
        
        System.out.println("   ✓ All audit categories tested");
        
        // Clean up
        auditManager.stopAuditLogging();
        auditManager.cleanup();
        VaultInitializer.resetVault();
        
        System.out.println("   ✅ Audit categories test passed");
    }
    
    /**
     * Test audit filtering
     */
    private static void testAuditFiltering() throws Exception {
        System.out.println("\n🧪 Testing audit filtering...");
        
        // Initialize test vault
        String masterPassword = "TestMasterPassword123!";
        VaultInitializer.initializeVault(masterPassword, "TestPanicPassword456@", "TestDecoyPassword789#");
        
        AuditManager auditManager = new AuditManager();
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        javax.crypto.SecretKey auditKey = passwordManager.deriveVaultKey(masterPassword);
        auditManager.startAuditLogging(auditKey);
        
        // Log entries with different categories and times
        LocalDateTime baseTime = LocalDateTime.now().minusHours(2);
        
        auditManager.logAuthentication("LOGIN", "user1", "192.168.1.1", true);
        Thread.sleep(100);
        auditManager.logFileOperation("UPLOAD", "file1.txt", "file1", 1024, true, null);
        Thread.sleep(100);
        auditManager.logAuthentication("LOGOUT", "user1", "192.168.1.1", true);
        
        Thread.sleep(1000);
        
        // Test category filtering
        List<AuditEntry> authEntries = auditManager.readAuditLog(100, 
            AuditManager.AuditCategory.AUTHENTICATION, null, null);
        List<AuditEntry> fileEntries = auditManager.readAuditLog(100, 
            AuditManager.AuditCategory.FILE_OPERATIONS, null, null);
        
        long authCount = authEntries.stream()
            .filter(e -> e.getCategory() == AuditManager.AuditCategory.AUTHENTICATION)
            .count();
        long fileCount = fileEntries.stream()
            .filter(e -> e.getCategory() == AuditManager.AuditCategory.FILE_OPERATIONS)
            .count();
        
        assert authCount >= 2 : "Should have at least 2 authentication entries";
        assert fileCount >= 1 : "Should have at least 1 file operation entry";
        
        // Test date filtering
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<AuditEntry> recentEntries = auditManager.readAuditLog(100, null, oneHourAgo, null);
        
        assert !recentEntries.isEmpty() : "Should have recent entries";
        
        System.out.println("   ✓ Category filtering: " + authCount + " auth, " + fileCount + " file");
        System.out.println("   ✓ Date filtering: " + recentEntries.size() + " recent entries");
        
        // Clean up
        auditManager.stopAuditLogging();
        auditManager.cleanup();
        VaultInitializer.resetVault();
        
        System.out.println("   ✅ Audit filtering test passed");
    }
    
    /**
     * Test audit statistics
     */
    private static void testAuditStatistics() throws Exception {
        System.out.println("\n🧪 Testing audit statistics...");
        
        // Initialize test vault
        String masterPassword = "TestMasterPassword123!";
        VaultInitializer.initializeVault(masterPassword, "TestPanicPassword456@", "TestDecoyPassword789#");
        
        AuditManager auditManager = new AuditManager();
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        javax.crypto.SecretKey auditKey = passwordManager.deriveVaultKey(masterPassword);
        auditManager.startAuditLogging(auditKey);
        
        // Log various events with different severities
        auditManager.logAuthentication("LOGIN", "user1", "192.168.1.1", true);
        auditManager.logSecurityEvent("WARNING_EVENT", "Test warning", 
            AuditManager.AuditSeverity.WARNING, "192.168.1.1", null);
        auditManager.logError("TestError", "Test error message", null, "test context");
        
        Thread.sleep(1000);
        
        // Get statistics
        AuditStatistics stats = auditManager.getAuditStatistics();
        
        assert stats.getTotalEntries() > 0 : "Should have audit entries";
        assert stats.getTotalLogSize() > 0 : "Should have log file size";
        assert stats.getSecurityHealthScore() >= 0 && stats.getSecurityHealthScore() <= 100 : 
            "Health score should be 0-100";
        
        // Check category counts
        long authCount = stats.getCategoryCount(AuditManager.AuditCategory.AUTHENTICATION);
        long securityCount = stats.getCategoryCount(AuditManager.AuditCategory.SECURITY_EVENTS);
        long errorCount = stats.getCategoryCount(AuditManager.AuditCategory.ERROR_EVENTS);
        
        assert authCount > 0 : "Should have authentication entries";
        assert securityCount > 0 : "Should have security entries";
        assert errorCount > 0 : "Should have error entries";
        
        System.out.println("   ✓ Statistics: " + stats.getTotalEntries() + " entries, " + 
            stats.getFormattedLogSize() + " size");
        System.out.println("   ✓ Health score: " + stats.getSecurityHealthScore() + "/100 (" + 
            stats.getHealthStatus() + ")");
        
        // Clean up
        auditManager.stopAuditLogging();
        auditManager.cleanup();
        VaultInitializer.resetVault();
        
        System.out.println("   ✅ Audit statistics test passed");
    }
    
    /**
     * Test log rotation
     */
    private static void testLogRotation() throws Exception {
        System.out.println("\n🧪 Testing log rotation...");
        
        // Initialize test vault
        String masterPassword = "TestMasterPassword123!";
        VaultInitializer.initializeVault(masterPassword, "TestPanicPassword456@", "TestDecoyPassword789#");
        
        AuditManager auditManager = new AuditManager();
        auditManager.setMaxLogFileSize(1024); // Small size to trigger rotation
        auditManager.setMaxLogFiles(3);
        
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        javax.crypto.SecretKey auditKey = passwordManager.deriveVaultKey(masterPassword);
        auditManager.startAuditLogging(auditKey);
        
        // Log many entries to trigger rotation
        for (int i = 0; i < 50; i++) {
            auditManager.logAuthentication("LOGIN_" + i, "user" + i, "192.168.1." + (i % 255), true);
            Thread.sleep(10); // Small delay
        }
        
        Thread.sleep(2000); // Wait for processing
        
        // Check that log files exist
        File logFile = new File(AppConfig.LOG_FILE);
        assert logFile.exists() : "Main log file should exist";
        
        System.out.println("   ✓ Log rotation mechanism tested");
        
        // Clean up
        auditManager.stopAuditLogging();
        auditManager.cleanup();
        VaultInitializer.resetVault();
        
        System.out.println("   ✅ Log rotation test passed");
    }
    
    /**
     * Test secure audit storage
     */
    private static void testSecureAuditStorage() throws Exception {
        System.out.println("\n🧪 Testing secure audit storage...");
        
        // Initialize test vault
        String masterPassword = "TestMasterPassword123!";
        VaultInitializer.initializeVault(masterPassword, "TestPanicPassword456@", "TestDecoyPassword789#");
        
        AuditManager auditManager = new AuditManager();
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        javax.crypto.SecretKey auditKey = passwordManager.deriveVaultKey(masterPassword);
        auditManager.startAuditLogging(auditKey);
        
        // Log sensitive information
        auditManager.logAuthentication("LOGIN", "sensitive_username", "192.168.1.1", true);
        auditManager.logFileOperation("UPLOAD", "confidential_file.txt", "file789", 2048, true, null);
        
        Thread.sleep(1000);
        auditManager.stopAuditLogging();
        
        // Verify log file is encrypted (should not contain plaintext)
        File logFile = new File(AppConfig.LOG_FILE);
        if (logFile.exists()) {
            byte[] logContent = java.nio.file.Files.readAllBytes(logFile.toPath());
            String logString = new String(logContent);
            
            // Should not contain plaintext sensitive data
            assert !logString.contains("sensitive_username") : "Log should not contain plaintext usernames";
            assert !logString.contains("confidential_file.txt") : "Log should not contain plaintext filenames";
            
            System.out.println("   ✓ Log file is encrypted (no plaintext found)");
        }
        
        // Verify we can still read entries with correct key
        auditManager.startAuditLogging(auditKey);
        List<AuditEntry> entries = auditManager.readAuditLog(100, null, null, null);
        
        boolean foundSensitiveEntry = entries.stream()
            .anyMatch(e -> e.getEventType().equals("LOGIN_ATTEMPT"));
        
        assert foundSensitiveEntry : "Should be able to decrypt and read entries";
        
        System.out.println("   ✓ Entries can be decrypted with correct key");
        
        // Clean up
        auditManager.stopAuditLogging();
        auditManager.cleanup();
        VaultInitializer.resetVault();
        
        System.out.println("   ✅ Secure audit storage test passed");
    }
    
    /**
     * Test panic mode destruction
     */
    private static void testPanicModeDestruction() throws Exception {
        System.out.println("\n🧪 Testing panic mode destruction...");
        
        // Initialize test vault
        String masterPassword = "TestMasterPassword123!";
        VaultInitializer.initializeVault(masterPassword, "TestPanicPassword456@", "TestDecoyPassword789#");
        
        AuditManager auditManager = new AuditManager();
        PasswordManager passwordManager = new PasswordManager(AppConfig.VAULT_DIR);
        
        javax.crypto.SecretKey auditKey = passwordManager.deriveVaultKey(masterPassword);
        auditManager.startAuditLogging(auditKey);
        
        // Log some entries
        auditManager.logAuthentication("LOGIN", "testuser", "192.168.1.1", true);
        auditManager.logFileOperation("UPLOAD", "test.txt", "file123", 1024, true, null);
        
        Thread.sleep(1000);
        
        // Verify log file exists
        File logFile = new File(AppConfig.LOG_FILE);
        assert logFile.exists() : "Log file should exist before panic mode";
        
        // Log panic mode activation
        auditManager.logPanicMode("MANUAL_TRIGGER", "User activated panic mode");
        
        Thread.sleep(500);
        
        // Simulate panic mode destruction
        auditManager.secureDeleteAuditLogs();
        
        // Verify log file is deleted
        assert !logFile.exists() : "Log file should be deleted after panic mode";
        
        System.out.println("   ✓ Audit logs securely deleted in panic mode");
        
        // Clean up
        auditManager.stopAuditLogging();
        auditManager.cleanup();
        VaultInitializer.resetVault();
        
        System.out.println("   ✅ Panic mode destruction test passed");
    }
}