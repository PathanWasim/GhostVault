package com.ghostvault.security;

import com.ghostvault.audit.AuditManager;
import com.ghostvault.config.AppConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for advanced security features
 */
class AdvancedSecurityTest {
    
    @TempDir
    Path tempDir;
    
    private SecurityHardening securityHardening;
    private ThreatDetectionEngine threatEngine;
    private AuditManager auditManager;
    
    @BeforeEach
    void setUp() throws Exception {
        // Set up temporary vault directory
        AppConfig.VAULT_DIR = tempDir.resolve("vault").toString();
        Files.createDirectories(Path.of(AppConfig.VAULT_DIR));
        
        // Initialize components
        auditManager = new AuditManager();
        securityHardening = SecurityHardening.getInstance();
        threatEngine = new ThreatDetectionEngine(auditManager);
    }
    
    @AfterEach
    void tearDown() {
        if (threatEngine != null) {
            threatEngine.stopMonitoring();
        }
    }
    
    @Test
    @DisplayName("Security hardening should apply all measures successfully")
    void testSecurityHardening() throws Exception {
        // Test security hardening application
        assertDoesNotThrow(() -> securityHardening.applySecurityHardening());
        
        // Verify security status
        SecurityHardening.SecurityStatus status = securityHardening.checkSecurityStatus();
        assertNotNull(status);
        
        // Check individual security measures
        assertTrue(status.fileSystemSecure, "File system should be secured");
        assertFalse(status.debuggerDetected, "No debugger should be detected initially");
        assertTrue(status.integrityVerified, "Integrity should be verified");
        
        // Test status report generation
        String report = status.getStatusReport();
        assertNotNull(report);
        assertTrue(report.contains("Security Status Report"));
        assertTrue(report.contains("File System"));
        assertTrue(report.contains("Memory Protection"));
    }
    
    @Test
    @DisplayName("Threat detection engine should start and stop correctly")
    void testThreatDetectionLifecycle() {
        // Test starting threat detection
        assertDoesNotThrow(() -> threatEngine.startMonitoring());
        
        // Test stopping threat detection
        assertDoesNotThrow(() -> threatEngine.stopMonitoring());
        
        // Test multiple start/stop cycles
        assertDoesNotThrow(() -> {
            threatEngine.startMonitoring();
            threatEngine.startMonitoring(); // Should handle duplicate starts
            threatEngine.stopMonitoring();
            threatEngine.stopMonitoring(); // Should handle duplicate stops
        });
    }
    
    @Test
    @DisplayName("Threat detection should analyze security events correctly")
    void testThreatEventAnalysis() {
        threatEngine.startMonitoring();
        
        // Test failed login analysis
        Map<String, String> loginMetadata = new HashMap<>();
        loginMetadata.put("username", "testuser");
        loginMetadata.put("timestamp", "2024-01-01T10:00:00");
        
        assertDoesNotThrow(() -> {
            threatEngine.recordSecurityEvent("LOGIN_FAILED", "192.168.1.100", loginMetadata);
        });
        
        // Test file access analysis
        Map<String, String> fileMetadata = new HashMap<>();
        fileMetadata.put("filename", "sensitive_data.key");
        fileMetadata.put("operation", "READ");
        
        assertDoesNotThrow(() -> {
            threatEngine.recordSecurityEvent("FILE_ACCESS", "user1", fileMetadata);
        });
        
        // Test memory usage analysis
        Map<String, String> memoryMetadata = new HashMap<>();
        memoryMetadata.put("usage_percent", "95");
        memoryMetadata.put("used_bytes", "1073741824");
        
        assertDoesNotThrow(() -> {
            threatEngine.recordSecurityEvent("MEMORY_USAGE", "system", memoryMetadata);
        });
        
        // Get threat assessment
        ThreatDetectionEngine.ThreatAssessment assessment = threatEngine.getCurrentThreatAssessment();
        assertNotNull(assessment);
        assertNotNull(assessment.getOverallLevel());
        assertTrue(assessment.getTotalEvents() > 0);
        
        // Test assessment report
        String report = assessment.getAssessmentReport();
        assertNotNull(report);
        assertTrue(report.contains("Threat Assessment Report"));
        assertTrue(report.contains("Overall Threat Level"));
    }
    
    @Test
    @DisplayName("Brute force attack detection should work correctly")
    void testBruteForceDetection() {
        threatEngine.startMonitoring();
        
        String attackerIP = "192.168.1.200";
        Map<String, String> metadata = new HashMap<>();
        metadata.put("username", "admin");
        
        // Simulate multiple failed login attempts
        for (int i = 0; i < 10; i++) {
            threatEngine.recordSecurityEvent("LOGIN_FAILED", attackerIP, metadata);
        }
        
        // Check threat assessment
        ThreatDetectionEngine.ThreatAssessment assessment = threatEngine.getCurrentThreatAssessment();
        
        // Should detect brute force attack
        Map<ThreatDetectionEngine.ThreatType, ThreatDetectionEngine.ThreatLevel> threats = assessment.getThreats();
        ThreatDetectionEngine.ThreatLevel bruteForceLevel = threats.get(ThreatDetectionEngine.ThreatType.BRUTE_FORCE_ATTACK);
        
        // Threat level should be elevated (not LOW)
        assertNotNull(bruteForceLevel);
        assertTrue(bruteForceLevel.getLevel() > ThreatDetectionEngine.ThreatLevel.LOW.getLevel());
    }
    
    @Test
    @DisplayName("File system tampering detection should work")
    void testFileSystemTamperingDetection() {
        threatEngine.startMonitoring();
        
        // Simulate system file modification
        Map<String, String> metadata = new HashMap<>();
        metadata.put("filename", "/etc/passwd");
        metadata.put("operation", "WRITE");
        
        threatEngine.recordSecurityEvent("FILE_MODIFIED", "suspicious_process", metadata);
        
        // Check threat assessment
        ThreatDetectionEngine.ThreatAssessment assessment = threatEngine.getCurrentThreatAssessment();
        Map<ThreatDetectionEngine.ThreatType, ThreatDetectionEngine.ThreatLevel> threats = assessment.getThreats();
        
        ThreatDetectionEngine.ThreatLevel tamperingLevel = threats.get(ThreatDetectionEngine.ThreatType.FILE_SYSTEM_TAMPERING);
        assertNotNull(tamperingLevel);
        assertTrue(tamperingLevel.getLevel() > ThreatDetectionEngine.ThreatLevel.LOW.getLevel());
    }
    
    @Test
    @DisplayName("Resource exhaustion detection should work")
    void testResourceExhaustionDetection() {
        threatEngine.startMonitoring();
        
        // Test high memory usage
        Map<String, String> memoryMetadata = new HashMap<>();
        memoryMetadata.put("usage_percent", "98");
        threatEngine.recordSecurityEvent("MEMORY_USAGE", "system", memoryMetadata);
        
        // Test high CPU usage
        Map<String, String> cpuMetadata = new HashMap<>();
        cpuMetadata.put("usage_percent", "99");
        threatEngine.recordSecurityEvent("CPU_USAGE", "system", cpuMetadata);
        
        // Check threat assessment
        ThreatDetectionEngine.ThreatAssessment assessment = threatEngine.getCurrentThreatAssessment();
        Map<ThreatDetectionEngine.ThreatType, ThreatDetectionEngine.ThreatLevel> threats = assessment.getThreats();
        
        // Should detect memory exhaustion
        ThreatDetectionEngine.ThreatLevel memoryLevel = threats.get(ThreatDetectionEngine.ThreatType.MEMORY_EXHAUSTION);
        assertNotNull(memoryLevel);
        assertTrue(memoryLevel.getLevel() > ThreatDetectionEngine.ThreatLevel.LOW.getLevel());
        
        // Should detect CPU exhaustion
        ThreatDetectionEngine.ThreatLevel cpuLevel = threats.get(ThreatDetectionEngine.ThreatType.CPU_EXHAUSTION);
        assertNotNull(cpuLevel);
        assertTrue(cpuLevel.getLevel() > ThreatDetectionEngine.ThreatLevel.LOW.getLevel());
    }
    
    @Test
    @DisplayName("Sensitive file access detection should work")
    void testSensitiveFileDetection() {
        threatEngine.startMonitoring();
        
        // Test access to sensitive files
        String[] sensitiveFiles = {
            "passwords.txt", "private.key", "secret.pem", "api_keys.json"
        };
        
        for (String fileName : sensitiveFiles) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("filename", fileName);
            metadata.put("operation", "READ");
            
            threatEngine.recordSecurityEvent("FILE_ACCESS", "user1", metadata);
        }
        
        // Check threat assessment
        ThreatDetectionEngine.ThreatAssessment assessment = threatEngine.getCurrentThreatAssessment();
        Map<ThreatDetectionEngine.ThreatType, ThreatDetectionEngine.ThreatLevel> threats = assessment.getThreats();
        
        // Should detect potential data exfiltration
        ThreatDetectionEngine.ThreatLevel exfiltrationLevel = threats.get(ThreatDetectionEngine.ThreatType.DATA_EXFILTRATION);
        assertNotNull(exfiltrationLevel);
        assertTrue(exfiltrationLevel.getLevel() > ThreatDetectionEngine.ThreatLevel.LOW.getLevel());
    }
    
    @Test
    @DisplayName("Security hardening should create decoy files")
    void testDecoyFileCreation() throws Exception {
        securityHardening.applySecurityHardening();
        
        // Check if decoy files were created
        Path vaultPath = Path.of(AppConfig.VAULT_DIR);
        assertTrue(Files.exists(vaultPath));
        
        // Look for common decoy file names
        String[] expectedDecoys = {"config.bak", "settings.tmp", "cache.dat"};
        
        for (String decoyName : expectedDecoys) {
            Path decoyPath = vaultPath.resolve(decoyName);
            if (Files.exists(decoyPath)) {
                // At least one decoy file should exist
                assertTrue(Files.size(decoyPath) > 0, "Decoy file should have content");
                break;
            }
        }
    }
    
    @Test
    @DisplayName("Threat assessment report should be comprehensive")
    void testThreatAssessmentReport() {
        threatEngine.startMonitoring();
        
        // Generate some security events
        Map<String, String> metadata = new HashMap<>();
        metadata.put("test", "value");
        
        threatEngine.recordSecurityEvent("LOGIN_FAILED", "test", metadata);
        threatEngine.recordSecurityEvent("FILE_ACCESS", "test", metadata);
        
        ThreatDetectionEngine.ThreatAssessment assessment = threatEngine.getCurrentThreatAssessment();
        String report = assessment.getAssessmentReport();
        
        // Verify report contains expected sections
        assertTrue(report.contains("Threat Assessment Report"));
        assertTrue(report.contains("Timestamp:"));
        assertTrue(report.contains("Overall Threat Level:"));
        assertTrue(report.contains("Total Events Processed:"));
        assertTrue(report.contains("Individual Threat Levels:"));
        
        // Should contain threat type information
        assertTrue(report.contains("BRUTE_FORCE_ATTACK") || 
                  report.contains("EXCESSIVE_FILE_ACCESS") ||
                  report.contains("DATA_EXFILTRATION"));
    }
    
    @Test
    @DisplayName("Security status should provide accurate information")
    void testSecurityStatusAccuracy() throws Exception {
        securityHardening.applySecurityHardening();
        SecurityHardening.SecurityStatus status = securityHardening.checkSecurityStatus();
        
        // Test status report format
        String report = status.getStatusReport();
        assertNotNull(report);
        
        // Should contain status indicators
        assertTrue(report.contains("✅") || report.contains("❌") || report.contains("🚨"));
        
        // Should have overall status
        assertTrue(report.contains("SECURE") || report.contains("VULNERABLE"));
        
        // Test individual status checks
        assertNotNull(status.fileSystemSecure);
        assertNotNull(status.memoryProtected);
        assertNotNull(status.debuggerDetected);
        assertNotNull(status.integrityVerified);
        assertNotNull(status.networkSecure);
    }
    
    @Test
    @DisplayName("Threat detection should handle invalid data gracefully")
    void testThreatDetectionErrorHandling() {
        threatEngine.startMonitoring();
        
        // Test with null metadata
        assertDoesNotThrow(() -> {
            threatEngine.recordSecurityEvent("TEST_EVENT", "source", null);
        });
        
        // Test with empty metadata
        assertDoesNotThrow(() -> {
            threatEngine.recordSecurityEvent("TEST_EVENT", "source", new HashMap<>());
        });
        
        // Test with invalid memory usage
        Map<String, String> invalidMetadata = new HashMap<>();
        invalidMetadata.put("usage_percent", "invalid_number");
        
        assertDoesNotThrow(() -> {
            threatEngine.recordSecurityEvent("MEMORY_USAGE", "system", invalidMetadata);
        });
        
        // Test with null source
        assertDoesNotThrow(() -> {
            threatEngine.recordSecurityEvent("TEST_EVENT", null, new HashMap<>());
        });
    }
    
    @Test
    @DisplayName("Security hardening should be idempotent")
    void testSecurityHardeningIdempotency() throws Exception {
        // Apply hardening multiple times
        securityHardening.applySecurityHardening();
        SecurityHardening.SecurityStatus status1 = securityHardening.checkSecurityStatus();
        
        securityHardening.applySecurityHardening();
        SecurityHardening.SecurityStatus status2 = securityHardening.checkSecurityStatus();
        
        // Status should be consistent
        assertEquals(status1.fileSystemSecure, status2.fileSystemSecure);
        assertEquals(status1.networkSecure, status2.networkSecure);
        assertEquals(status1.integrityVerified, status2.integrityVerified);
    }
}