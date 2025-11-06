package com.ghostvault.ui;

import com.ghostvault.core.FileManager;
import com.ghostvault.core.MetadataManager;
import com.ghostvault.backup.VaultBackupManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.nio.file.Path;
import java.lang.reflect.Method;

/**
 * Integration tests for VaultMainController password integration
 * Tests the password passing and validation in the controller
 */
public class VaultMainControllerPasswordIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    @Mock
    private FileManager mockFileManager;
    
    @Mock
    private MetadataManager mockMetadataManager;
    
    @Mock
    private VaultBackupManager mockBackupManager;
    
    private VaultMainController controller;
    private SecretKey testEncryptionKey;
    
    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create test encryption key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        testEncryptionKey = keyGen.generateKey();
        
        // Create controller instance
        controller = new VaultMainController();
        
        // Setup mock behaviors
        when(mockFileManager.isPasswordAvailable()).thenReturn(false);
        when(mockFileManager.isEncryptionEnabled()).thenReturn(true);
        when(mockFileManager.isReadyForEncryptedOperations()).thenReturn(false);
        
        doNothing().when(mockFileManager).setPassword(anyString());
        doNothing().when(mockFileManager).setEncryptionEnabled(anyBoolean());
        doNothing().when(mockFileManager).setEncryptionKey(any(SecretKey.class));
        
        doNothing().when(mockMetadataManager).setPassword(anyString());
        doNothing().when(mockMetadataManager).setEncryptionEnabled(anyBoolean());
        doNothing().when(mockMetadataManager).setEncryptionKey(any(SecretKey.class));
        doNothing().when(mockMetadataManager).loadMetadata();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up
        controller = null;
    }
    
    @Test
    void testInitializeWithPasswordPreferred() throws Exception {
        // Test that initialize method prefers password-based methods when available
        
        // Mock getSessionPassword to return a password
        VaultMainController spyController = spy(controller);
        doReturn("testPassword123").when(spyController).getSessionPassword();
        
        // Initialize with mocked managers
        spyController.initialize(mockFileManager, mockMetadataManager, mockBackupManager, testEncryptionKey);
        
        // Verify password-based methods were called
        verify(mockFileManager).setPassword("testPassword123");
        verify(mockFileManager).setEncryptionEnabled(true);
        verify(mockMetadataManager).setPassword("testPassword123");
        verify(mockMetadataManager).setEncryptionEnabled(true);
        
        // Verify legacy methods were NOT called when password is available
        verify(mockFileManager, never()).setEncryptionKey(any(SecretKey.class));
        verify(mockMetadataManager, never()).setEncryptionKey(any(SecretKey.class));
    }
    
    @Test
    void testInitializeFallsBackToLegacyMethods() throws Exception {
        // Test that initialize method falls back to legacy methods when password not available
        
        // Mock getSessionPassword to return null (no password available)
        VaultMainController spyController = spy(controller);
        doReturn(null).when(spyController).getSessionPassword();
        
        // Initialize with mocked managers
        spyController.initialize(mockFileManager, mockMetadataManager, mockBackupManager, testEncryptionKey);
        
        // Verify legacy methods were called as fallback
        verify(mockFileManager).setEncryptionKey(testEncryptionKey);
        verify(mockMetadataManager).setEncryptionKey(testEncryptionKey);
        
        // Verify password-based methods were NOT called when password not available
        verify(mockFileManager, never()).setPassword(anyString());
        verify(mockMetadataManager, never()).setPassword(anyString());
    }
    
    @Test
    void testPasswordBasedInitializationWithException() throws Exception {
        // Test handling of exceptions during password-based initialization
        
        VaultMainController spyController = spy(controller);
        doReturn("testPassword123").when(spyController).getSessionPassword();
        
        // Mock FileManager to throw exception on setPassword
        doThrow(new RuntimeException("Password setup failed")).when(mockFileManager).setPassword(anyString());
        
        // Initialize should not throw exception, should fall back to legacy method
        assertDoesNotThrow(() -> {
            spyController.initialize(mockFileManager, mockMetadataManager, mockBackupManager, testEncryptionKey);
        });
        
        // Verify fallback to legacy method occurred
        verify(mockFileManager).setEncryptionKey(testEncryptionKey);
        
        // Verify MetadataManager still got password (FileManager exception shouldn't affect it)
        verify(mockMetadataManager).setPassword("testPassword123");
    }
    
    @Test
    void testGetSessionPasswordMethod() throws Exception {
        // Test the getSessionPassword method using reflection
        
        Method getSessionPasswordMethod = VaultMainController.class.getDeclaredMethod("getSessionPassword");
        getSessionPasswordMethod.setAccessible(true);
        
        // Call the method
        String result = (String) getSessionPasswordMethod.invoke(controller);
        
        // Currently should return null as it's not implemented
        assertNull(result, "getSessionPassword should return null when not implemented");
    }
    
    @Test
    void testInitializeWithNullManagers() throws Exception {
        // Test initialize method with null managers
        
        VaultMainController spyController = spy(controller);
        doReturn("testPassword123").when(spyController).getSessionPassword();
        
        // Should not throw exception with null managers
        assertDoesNotThrow(() -> {
            spyController.initialize(null, null, null, testEncryptionKey);
        });
        
        // Should not throw exception with some null managers
        assertDoesNotThrow(() -> {
            spyController.initialize(mockFileManager, null, mockBackupManager, testEncryptionKey);
        });
        
        assertDoesNotThrow(() -> {
            spyController.initialize(null, mockMetadataManager, mockBackupManager, testEncryptionKey);
        });
    }
    
    @Test
    void testInitializeWithNullEncryptionKey() throws Exception {
        // Test initialize method with null encryption key
        
        VaultMainController spyController = spy(controller);
        doReturn(null).when(spyController).getSessionPassword(); // No password available
        
        // Should not throw exception with null encryption key
        assertDoesNotThrow(() -> {
            spyController.initialize(mockFileManager, mockMetadataManager, mockBackupManager, null);
        });
        
        // Verify no encryption methods were called
        verify(mockFileManager, never()).setEncryptionKey(any(SecretKey.class));
        verify(mockMetadataManager, never()).setEncryptionKey(any(SecretKey.class));
        verify(mockFileManager, never()).setPassword(anyString());
        verify(mockMetadataManager, never()).setPassword(anyString());
    }
    
    @Test
    void testMetadataManagerPasswordExceptionHandling() throws Exception {
        // Test handling of MetadataManager password setup exceptions
        
        VaultMainController spyController = spy(controller);
        doReturn("testPassword123").when(spyController).getSessionPassword();
        
        // Mock MetadataManager to throw exception on setPassword
        doThrow(new RuntimeException("Metadata password setup failed")).when(mockMetadataManager).setPassword(anyString());
        
        // Initialize should not throw exception
        assertDoesNotThrow(() -> {
            spyController.initialize(mockFileManager, mockMetadataManager, mockBackupManager, testEncryptionKey);
        });
        
        // Verify FileManager still got password (MetadataManager exception shouldn't affect it)
        verify(mockFileManager).setPassword("testPassword123");
        
        // Verify fallback to legacy method for MetadataManager
        verify(mockMetadataManager).setEncryptionKey(testEncryptionKey);
    }
    
    @Test
    void testMetadataLoadingWithPasswordBasedMethod() throws Exception {
        // Test metadata loading with password-based method
        
        VaultMainController spyController = spy(controller);
        doReturn("testPassword123").when(spyController).getSessionPassword();
        
        // Initialize
        spyController.initialize(mockFileManager, mockMetadataManager, mockBackupManager, testEncryptionKey);
        
        // Verify metadata loading was attempted
        verify(mockMetadataManager).loadMetadata();
    }
    
    @Test
    void testMetadataLoadingExceptionHandling() throws Exception {
        // Test handling of metadata loading exceptions
        
        VaultMainController spyController = spy(controller);
        doReturn("testPassword123").when(spyController).getSessionPassword();
        
        // Mock metadata loading to throw exception
        doThrow(new RuntimeException("Metadata loading failed")).when(mockMetadataManager).loadMetadata();
        
        // Initialize should not throw exception
        assertDoesNotThrow(() -> {
            spyController.initialize(mockFileManager, mockMetadataManager, mockBackupManager, testEncryptionKey);
        });
        
        // Verify password was still set despite loading failure
        verify(mockMetadataManager).setPassword("testPassword123");
    }
}