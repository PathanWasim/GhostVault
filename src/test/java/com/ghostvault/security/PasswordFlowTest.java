package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for password flow with timing parity verification
 */
@DisplayName("Password Flow Tests")
class PasswordFlowTest {
    
    @TempDir
    Path tempDir;
    
    private PasswordManager passwordManager;
    private char[] masterPassword;
    private char[] panicPassword;
    private char[] decoyPassword;
    
    @BeforeEach
    void setUp() throws Exception {
        // Set vault path to temp directory
        System.setProperty("user.home", tempDir.toString());
        
        passwordManager = new PasswordManager(tempDir.toString());
        
        // Create test passwords
        masterPassword = "MasterPass123!@#".toCharArray();
        panicPassword = "PanicPass456$%^".toCharArray();
        decoyPassword = "DecoyPass789&*(".toCharArray();
        
        // Initialize passwords
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
    }
    
    @Test
    @DisplayName("Should detect master password correctly")
    void testMasterPasswordDetection() throws Exception {
        // Act
        PasswordManager.PasswordType type = passwordManager.detectPassword(masterPassword);
        
        // Assert
        assertEquals(PasswordManager.PasswordType.MASTER, type);
    }
    
    @Test
    @DisplayName("Should detect panic password correctly")
    void testPanicPasswordDetection() throws Exception {
        // Act
        PasswordManager.PasswordType type = passwordManager.detectPassword(panicPassword);
        
        // Assert
        assertEquals(PasswordManager.PasswordType.PANIC, type);
    }
    
    @Test
    @DisplayName("Should detect decoy password correctly")
    void testDecoyPasswordDetection() throws Exception {
        // Act
        PasswordManager.PasswordType type = passwordManager.detectPassword(decoyPassword);
        
        // Assert
        assertEquals(PasswordManager.PasswordType.DECOY, type);
    }
    
    @Test
    @DisplayName("Should return INVALID for wrong password")
    void testInvalidPasswordDetection() throws Exception {
        // Arrange
        char[] wrongPassword = "WrongPassword123".toCharArray();
        
        // Act
        PasswordManager.PasswordType type = passwordManager.detectPassword(wrongPassword);
        
        // Assert
        assertEquals(PasswordManager.PasswordType.INVALID, type);
    }
    
    @Test
    @DisplayName("Should have timing parity across password types")
    void testTimingParity() throws Exception {
        // Arrange
        int iterations = 10; // Reduced for faster testing
        List<Long> masterTimes = new ArrayList<>();
        List<Long> panicTimes = new ArrayList<>();
        List<Long> decoyTimes = new ArrayList<>();
        List<Long> invalidTimes = new ArrayList<>();
        
        char[] invalidPassword = "InvalidPass123".toCharArray();
        
        // Act - Measure timing for each password type
        for (int i = 0; i < iterations; i++) {
            // Master password
            long start = System.nanoTime();
            passwordManager.detectPassword(masterPassword);
            long duration = System.nanoTime() - start;
            masterTimes.add(duration);
            
            // Panic password
            start = System.nanoTime();
            passwordManager.detectPassword(panicPassword);
            duration = System.nanoTime() - start;
            panicTimes.add(duration);
            
            // Decoy password
            start = System.nanoTime();
            passwordManager.detectPassword(decoyPassword);
            duration = System.nanoTime() - start;
            decoyTimes.add(duration);
            
            // Invalid password
            start = System.nanoTime();
            passwordManager.detectPassword(invalidPassword);
            duration = System.nanoTime() - start;
            invalidTimes.add(duration);
        }
        
        // Calculate averages
        long avgMaster = average(masterTimes);
        long avgPanic = average(panicTimes);
        long avgDecoy = average(decoyTimes);
        long avgInvalid = average(invalidTimes);
        
        System.out.println("Average timings (ms):");
        System.out.println("  Master:  " + (avgMaster / 1_000_000.0));
        System.out.println("  Panic:   " + (avgPanic / 1_000_000.0));
        System.out.println("  Decoy:   " + (avgDecoy / 1_000_000.0));
        System.out.println("  Invalid: " + (avgInvalid / 1_000_000.0));
        
        // Assert - All timings should be within 100ms of each other
        // (accounting for the 900ms + 0-300ms jitter = 900-1200ms range)
        long maxDiff = 100_000_000L; // 100ms in nanoseconds
        
        assertTrue(Math.abs(avgMaster - avgPanic) < maxDiff,
            "Master and Panic timing difference too large");
        assertTrue(Math.abs(avgMaster - avgDecoy) < maxDiff,
            "Master and Decoy timing difference too large");
        assertTrue(Math.abs(avgMaster - avgInvalid) < maxDiff,
            "Master and Invalid timing difference too large");
    }
    
    @Test
    @DisplayName("Should unwrap VMK with master password")
    void testUnwrapVMK() throws Exception {
        // Act
        javax.crypto.SecretKey vmk = passwordManager.unwrapVMK(masterPassword);
        
        // Assert
        assertNotNull(vmk);
        assertEquals("AES", vmk.getAlgorithm());
        assertEquals(32, vmk.getEncoded().length);
    }
    
    @Test
    @DisplayName("Should unwrap DVMK with decoy password")
    void testUnwrapDVMK() throws Exception {
        // Act
        javax.crypto.SecretKey dvmk = passwordManager.unwrapDVMK(decoyPassword);
        
        // Assert
        assertNotNull(dvmk);
        assertEquals("AES", dvmk.getAlgorithm());
        assertEquals(32, dvmk.getEncoded().length);
    }
    
    @Test
    @DisplayName("Should fail to unwrap VMK with wrong password")
    void testUnwrapVMKWithWrongPassword() {
        // Arrange
        char[] wrongPassword = "WrongPassword123".toCharArray();
        
        // Assert
        assertThrows(Exception.class, () -> {
            passwordManager.unwrapVMK(wrongPassword);
        });
    }
    
    @Test
    @DisplayName("Should fail to unwrap DVMK with wrong password")
    void testUnwrapDVMKWithWrongPassword() {
        // Arrange
        char[] wrongPassword = "WrongPassword123".toCharArray();
        
        // Assert
        assertThrows(Exception.class, () -> {
            passwordManager.unwrapDVMK(wrongPassword);
        });
    }
    
    @Test
    @DisplayName("Should reject weak passwords during initialization")
    void testWeakPasswordRejection() {
        // Arrange
        char[] weakMaster = "weak".toCharArray();
        char[] weakPanic = "123".toCharArray();
        char[] weakDecoy = "abc".toCharArray();
        
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordManager pm = new PasswordManager(tempDir.toString());
            pm.initializePasswords(weakMaster, weakPanic, weakDecoy);
        });
    }
    
    @Test
    @DisplayName("Should reject duplicate passwords during initialization")
    void testDuplicatePasswordRejection() {
        // Arrange
        char[] samePassword = "SamePassword123!".toCharArray();
        
        // Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordManager pm = new PasswordManager(tempDir.toString());
            pm.initializePasswords(samePassword, samePassword, decoyPassword);
        });
    }
    
    @Test
    @DisplayName("Should persist and reload configuration")
    void testConfigurationPersistence() throws Exception {
        // Act - Create new PasswordManager instance (should reload config)
        PasswordManager newPM = new PasswordManager(tempDir.toString());
        
        // Assert
        assertTrue(newPM.arePasswordsConfigured());
        assertEquals(PasswordManager.PasswordType.MASTER, 
                    newPM.detectPassword(masterPassword));
        assertEquals(PasswordManager.PasswordType.PANIC, 
                    newPM.detectPassword(panicPassword));
        assertEquals(PasswordManager.PasswordType.DECOY, 
                    newPM.detectPassword(decoyPassword));
    }
    
    @Test
    @DisplayName("Should securely destroy all password data")
    void testSecureDestroy() throws Exception {
        // Act
        passwordManager.secureDestroy();
        
        // Assert
        assertFalse(passwordManager.arePasswordsConfigured());
        
        // Should return INVALID for all passwords after destruction
        assertEquals(PasswordManager.PasswordType.INVALID,
                    passwordManager.detectPassword(masterPassword));
    }
    
    @Test
    @DisplayName("Should calculate password strength correctly")
    void testPasswordStrength() {
        // Test various password strengths
        assertEquals(0, PasswordManager.getPasswordStrength(""));
        assertEquals(1, PasswordManager.getPasswordStrength("short"));
        assertEquals(2, PasswordManager.getPasswordStrength("Short123"));
        assertEquals(3, PasswordManager.getPasswordStrength("Short123!"));
        assertEquals(4, PasswordManager.getPasswordStrength("LongerPass123!"));
        assertEquals(5, PasswordManager.getPasswordStrength("VeryLongPassword123!@#"));
    }
    
    /**
     * Calculate average of timing measurements
     */
    private long average(List<Long> times) {
        return times.stream()
                   .mapToLong(Long::longValue)
                   .sum() / times.size();
    }
}
