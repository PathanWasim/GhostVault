package com.ghostvault.ui;

import com.ghostvault.config.AppConfig;
import com.ghostvault.security.PasswordManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for InitialSetupController
 */
public class InitialSetupTest {
    
    @TempDir
    Path tempDir;
    
    private PasswordManager passwordManager;
    private String vaultPath;
    
    @BeforeEach
    void setUp() throws Exception {
        vaultPath = tempDir.toString();
        passwordManager = new PasswordManager(vaultPath);
    }
    
    @Test
    @DisplayName("Test password initialization with valid passwords")
    void testPasswordInitialization() throws Exception {
        String masterPassword = "Master@Password123";
        String panicPassword = "Panic@Password456";
        String decoyPassword = "Decoy@Password789";
        
        passwordManager.initializePasswords(masterPassword, panicPassword, decoyPassword);
        
        // Verify passwords are validated correctly
        assertEquals(PasswordManager.PasswordType.MASTER, 
            passwordManager.validatePassword(masterPassword));
        assertEquals(PasswordManager.PasswordType.PANIC, 
            passwordManager.validatePassword(panicPassword));
        assertEquals(PasswordManager.PasswordType.DECOY, 
            passwordManager.validatePassword(decoyPassword));
    }
    
    @Test
    @DisplayName("Test password strength validation")
    void testPasswordStrength() {
        // Weak passwords should be rejected
        assertFalse(isStrongPassword("weak"));
        assertFalse(isStrongPassword("12345678"));
        assertFalse(isStrongPassword("password"));
        
        // Strong passwords should be accepted
        assertTrue(isStrongPassword("Strong@Pass123"));
        assertTrue(isStrongPassword("Complex#Password789!"));
    }
    
    @Test
    @DisplayName("Test duplicate password detection")
    void testDuplicatePasswords() throws Exception {
        String password = "Same@Password123";
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordManager.initializePasswords(password, password, "Different@123");
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            passwordManager.initializePasswords(password, "Different@123", password);
        });
    }
    
    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
