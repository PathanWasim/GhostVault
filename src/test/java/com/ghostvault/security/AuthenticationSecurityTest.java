package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive security tests for authentication system
 */
public class AuthenticationSecurityTest {
    
    private SecureAuthenticationManager authManager;
    
    @BeforeEach
    void setUp() {
        authManager = new SecureAuthenticationManager();
    }
    
    @Test
    @DisplayName("Master password should only open master vault")
    void testMasterPasswordOnlyOpensMasterVault() {
        // Test master password
        AuthenticationResult result = authManager.authenticate("masterpass123");
        
        assertTrue(result.isSuccess(), "Master password should be accepted");
        assertEquals(VaultMode.MASTER, result.getMode(), "Master password should open master vault");
        assertFalse(result.isPanicMode(), "Master password should not trigger panic mode");
    }
    
    @Test
    @DisplayName("Decoy password should only open decoy vault")
    void testDecoyPasswordOnlyOpensDecoyVault() {
        // Test decoy password
        AuthenticationResult result = authManager.authenticate("decoypass456");
        
        assertTrue(result.isSuccess(), "Decoy password should be accepted");
        assertEquals(VaultMode.DECOY, result.getMode(), "Decoy password should open decoy vault");
        assertFalse(result.isPanicMode(), "Decoy password should not trigger panic mode");
    }
    
    @Test
    @DisplayName("Panic password should trigger panic mode and not open vault")
    void testPanicPasswordTriggersWipe() {
        // Test panic password
        AuthenticationResult result = authManager.authenticate("panicmode999");
        
        assertTrue(result.isSuccess(), "Panic password should be accepted");
        assertEquals(VaultMode.PANIC, result.getMode(), "Panic password should return panic mode");
        assertTrue(result.isPanicMode(), "Panic password should trigger panic mode");
    }
    
    @Test
    @DisplayName("Invalid passwords should be rejected")
    void testInvalidPasswordsRejected() {
        String[] invalidPasswords = {
            "wrongpassword",
            "masterpass124", // Close but wrong
            "decoypass457",  // Close but wrong
            "panicmode998",  // Close but wrong
            "",              // Empty
            "admin",         // Common weak password
            "password123"    // Common weak password
        };
        
        for (String password : invalidPasswords) {
            AuthenticationResult result = authManager.authenticate(password);
            
            assertFalse(result.isSuccess(), "Invalid password should be rejected: " + password);
            assertNull(result.getMode(), "Invalid password should not return a mode: " + password);
            assertFalse(result.isPanicMode(), "Invalid password should not trigger panic: " + password);
        }
    }
    
    @Test
    @DisplayName("System should lock after 5 failed attempts")
    void testSystemLockoutAfterFailedAttempts() {
        // Make 5 failed attempts
        for (int i = 1; i <= 5; i++) {
            AuthenticationResult result = authManager.authenticate("wrongpassword" + i);
            
            assertFalse(result.isSuccess(), "Attempt " + i + " should fail");
            
            if (i < 5) {
                assertEquals(5 - i, result.getRemainingAttempts(), 
                    "Should have " + (5 - i) + " attempts remaining after attempt " + i);
            } else {
                assertEquals(0, result.getRemainingAttempts(), 
                    "Should have 0 attempts remaining after 5 failed attempts");
                assertTrue(result.getErrorMessage().contains("locked"), 
                    "Error message should indicate system is locked");
            }
        }
        
        // Verify system is locked
        assertTrue(authManager.isSystemLocked(), "System should be locked after 5 failed attempts");
        
        // Try valid password while locked - should still be rejected
        AuthenticationResult lockedResult = authManager.authenticate("masterpass123");
        assertFalse(lockedResult.isSuccess(), "Valid password should be rejected while system is locked");
        assertTrue(lockedResult.getErrorMessage().contains("locked"), 
            "Error message should indicate system is locked");
    }
    
    @Test
    @DisplayName("Password validation should be case sensitive")
    void testPasswordCaseSensitivity() {
        String[] caseSensitiveTests = {
            "MASTERPASS123",  // All caps
            "MasterPass123",  // Mixed case
            "masterPASS123",  // Mixed case
            "DECOYPASS456",   // All caps
            "DecoyPass456",   // Mixed case
            "PANICMODE999",   // All caps
            "PanicMode999"    // Mixed case
        };
        
        for (String password : caseSensitiveTests) {
            AuthenticationResult result = authManager.authenticate(password);
            
            assertFalse(result.isSuccess(), 
                "Case-sensitive password should be rejected: " + password);
        }
    }
    
    @Test
    @DisplayName("System should reset failed attempts after successful authentication")
    void testFailedAttemptsResetAfterSuccess() {
        // Make 3 failed attempts
        for (int i = 1; i <= 3; i++) {
            AuthenticationResult result = authManager.authenticate("wrongpassword" + i);
            assertFalse(result.isSuccess(), "Attempt " + i + " should fail");
        }
        
        // Verify we have 2 attempts remaining
        AuthenticationResult failResult = authManager.authenticate("wrongpassword4");
        assertFalse(failResult.isSuccess());
        assertEquals(1, failResult.getRemainingAttempts(), "Should have 1 attempt remaining");
        
        // Successful authentication should reset counter
        AuthenticationResult successResult = authManager.authenticate("masterpass123");
        assertTrue(successResult.isSuccess(), "Valid password should succeed");
        
        // Verify counter is reset by making another failed attempt
        AuthenticationResult resetTest = authManager.authenticate("wrongpassword");
        assertFalse(resetTest.isSuccess());
        assertEquals(4, resetTest.getRemainingAttempts(), 
            "Failed attempts counter should be reset after successful authentication");
    }
    
    @Test
    @DisplayName("Empty and null passwords should be rejected")
    void testEmptyAndNullPasswordsRejected() {
        // Test empty password
        AuthenticationResult emptyResult = authManager.authenticate("");
        assertFalse(emptyResult.isSuccess(), "Empty password should be rejected");
        assertTrue(emptyResult.getErrorMessage().contains("empty"), 
            "Error message should mention empty password");
        
        // Test null password
        AuthenticationResult nullResult = authManager.authenticate(null);
        assertFalse(nullResult.isSuccess(), "Null password should be rejected");
        assertTrue(nullResult.getErrorMessage().contains("empty"), 
            "Error message should mention empty password");
    }
    
    @Test
    @DisplayName("Authentication should be consistent across multiple calls")
    void testAuthenticationConsistency() {
        // Test same password multiple times
        for (int i = 0; i < 10; i++) {
            AuthenticationResult masterResult = authManager.authenticate("masterpass123");
            assertTrue(masterResult.isSuccess(), "Master password should always succeed");
            assertEquals(VaultMode.MASTER, masterResult.getMode(), "Should always return master mode");
            
            AuthenticationResult decoyResult = authManager.authenticate("decoypass456");
            assertTrue(decoyResult.isSuccess(), "Decoy password should always succeed");
            assertEquals(VaultMode.DECOY, decoyResult.getMode(), "Should always return decoy mode");
            
            AuthenticationResult invalidResult = authManager.authenticate("invalid");
            assertFalse(invalidResult.isSuccess(), "Invalid password should always fail");
        }
    }
}