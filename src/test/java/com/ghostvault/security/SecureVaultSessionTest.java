package com.ghostvault.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

/**
 * Integration tests for SecureVaultSession
 */
public class SecureVaultSessionTest {
    
    @TempDir
    Path tempDir;
    
    private SecureVaultSession vaultSession;
    private SecurePasswordStorage passwordStorage;
    private final String masterPassword = "MasterPass123!";
    private final String decoyPassword = "DecoyPass456@";
    private final String panicPassword = "PanicPass789#";
    
    @BeforeEach
    void setUp() throws Exception {
        vaultSession = new SecureVaultSession(tempDir.toString());
        passwordStorage = new SecurePasswordStorage(tempDir.toString());
        
        // Set up test passwords
        passwordStorage.storePasswordHashes(masterPassword, decoyPassword, panicPassword);
    }
    
    @Test
    @DisplayName("Should authenticate and initialize secure session")
    void shouldAuthenticateAndInitializeSecureSession() {
        assertFalse(vaultSession.isAuthenticated());
        
        AuthenticationResult result = vaultSession.authenticate(masterPassword);
        
        assertTrue(result.isSuccess());
        assertEquals(VaultMode.MASTER, result.getMode());
        assertTrue(vaultSession.isAuthenticated());
        assertEquals(SecurePasswordStorage.PasswordType.MASTER, vaultSession.getAuthenticatedType());
        assertEquals(VaultMode.MASTER, vaultSession.getCurrentMode());
    }
    
    @Test
    @DisplayName("Should handle different password types")
    void shouldHandleDifferentPasswordTypes() {
        // Test master password
        AuthenticationResult masterResult = vaultSession.authenticate(masterPassword);
        assertTrue(masterResult.isSuccess());
        assertEquals(VaultMode.MASTER, masterResult.getMode());
        
        vaultSession.logout();
        
        // Test decoy password
        AuthenticationResult decoyResult = vaultSession.authenticate(decoyPassword);
        assertTrue(decoyResult.isSuccess());
        assertEquals(VaultMode.DECOY, decoyResult.getMode());
        
        vaultSession.logout();
        
        // Test panic password
        AuthenticationResult panicResult = vaultSession.authenticate(panicPassword);
        assertTrue(panicResult.isSuccess());
        assertEquals(VaultMode.PANIC, panicResult.getMode());
    }
    
    @Test
    @DisplayName("Should provide access to vault components after authentication")
    void shouldProvideAccessToVaultComponentsAfterAuthentication() {
        vaultSession.authenticate(masterPassword);
        
        assertNotNull(vaultSession.getFileManager());
        assertNotNull(vaultSession.getMetadataManager());
        assertNotNull(vaultSession.getPrimaryEncryptionKey());
    }
    
    @Test
    @DisplayName("Should deny access to vault components before authentication")
    void shouldDenyAccessToVaultComponentsBeforeAuthentication() {
        assertThrows(IllegalStateException.class, () -> {
            vaultSession.getFileManager();
        });
        
        assertThrows(IllegalStateException.class, () -> {
            vaultSession.getMetadataManager();
        });
        
        assertThrows(IllegalStateException.class, () -> {
            vaultSession.getPrimaryEncryptionKey();
        });
    }
    
    @Test
    @DisplayName("Should logout and clear session data")
    void shouldLogoutAndClearSessionData() {
        vaultSession.authenticate(masterPassword);
        
        assertTrue(vaultSession.isAuthenticated());
        assertNotNull(vaultSession.getCurrentMode());
        
        vaultSession.logout();
        
        assertFalse(vaultSession.isAuthenticated());
        assertNull(vaultSession.getAuthenticatedType());
        assertNull(vaultSession.getCurrentMode());
        
        // Should not be able to access components after logout
        assertThrows(IllegalStateException.class, () -> {
            vaultSession.getFileManager();
        });
    }
    
    @Test
    @DisplayName("Should handle panic mode correctly")
    void shouldHandlePanicModeCorrectly() {
        vaultSession.authenticate(masterPassword);
        
        assertTrue(vaultSession.isAuthenticated());
        
        vaultSession.panicMode();
        
        assertFalse(vaultSession.isAuthenticated());
        assertEquals(VaultMode.PANIC, vaultSession.getCurrentMode());
        
        // Should not be able to access components after panic mode
        assertThrows(IllegalStateException.class, () -> {
            vaultSession.getFileManager();
        });
    }
    
    @Test
    @DisplayName("Should validate session integrity")
    void shouldValidateSessionIntegrity() {
        // Before authentication
        assertFalse(vaultSession.validateSessionIntegrity());
        
        // After authentication
        vaultSession.authenticate(masterPassword);
        assertTrue(vaultSession.validateSessionIntegrity());
        
        // After logout
        vaultSession.logout();
        assertFalse(vaultSession.validateSessionIntegrity());
    }
    
    @Test
    @DisplayName("Should provide session statistics")
    void shouldProvideSessionStatistics() {
        SecureVaultSession.SessionStats stats = vaultSession.getSessionStats();
        
        assertFalse(stats.isAuthenticated());
        assertNull(stats.getAuthenticatedType());
        assertNull(stats.getCurrentMode());
        assertNotNull(stats.getKeyStats());
        assertNotNull(stats.getMemoryStats());
        
        vaultSession.authenticate(masterPassword);
        
        stats = vaultSession.getSessionStats();
        assertTrue(stats.isAuthenticated());
        assertEquals(SecurePasswordStorage.PasswordType.MASTER, stats.getAuthenticatedType());
        assertEquals(VaultMode.MASTER, stats.getCurrentMode());
    }
    
    @Test
    @DisplayName("Should handle authentication failures")
    void shouldHandleAuthenticationFailures() {
        AuthenticationResult result = vaultSession.authenticate("WrongPassword");
        
        assertFalse(result.isSuccess());
        assertFalse(vaultSession.isAuthenticated());
        assertNull(vaultSession.getAuthenticatedType());
        assertNull(vaultSession.getCurrentMode());
    }
    
    @Test
    @DisplayName("Should handle multiple authentication attempts")
    void shouldHandleMultipleAuthenticationAttempts() {
        // First authentication
        vaultSession.authenticate(masterPassword);
        assertTrue(vaultSession.isAuthenticated());
        assertEquals(VaultMode.MASTER, vaultSession.getCurrentMode());
        
        // Logout and re-authenticate with different password
        vaultSession.logout();
        vaultSession.authenticate(decoyPassword);
        
        assertTrue(vaultSession.isAuthenticated());
        assertEquals(VaultMode.DECOY, vaultSession.getCurrentMode());
        assertEquals(SecurePasswordStorage.PasswordType.DECOY, vaultSession.getAuthenticatedType());
    }
    
    @Test
    @DisplayName("Should maintain session security across operations")
    void shouldMaintainSessionSecurityAcrossOperations() {
        vaultSession.authenticate(masterPassword);
        
        // Get components
        var fileManager = vaultSession.getFileManager();
        var metadataManager = vaultSession.getMetadataManager();
        var encryptionKey = vaultSession.getPrimaryEncryptionKey();
        
        assertNotNull(fileManager);
        assertNotNull(metadataManager);
        assertNotNull(encryptionKey);
        
        // Session should remain valid
        assertTrue(vaultSession.validateSessionIntegrity());
        
        // Components should be configured for encryption
        assertTrue(fileManager.isEncryptionEnabled());
        assertTrue(metadataManager.isEncryptionEnabled());
    }
    
    @Test
    @DisplayName("Should handle session without vault path")
    void shouldHandleSessionWithoutVaultPath() {
        SecureVaultSession defaultSession = new SecureVaultSession();
        
        // Should still be able to authenticate (will use default paths)
        // This test assumes the default vault setup exists
        assertFalse(defaultSession.isAuthenticated());
        
        // Cleanup
        defaultSession.logout();
    }
}