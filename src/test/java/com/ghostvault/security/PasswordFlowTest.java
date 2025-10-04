package com.ghostvault.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test to verify the complete password flow works correctly
 */
public class PasswordFlowTest {
    
    private static final String TEST_VAULT_PATH = System.getProperty("java.io.tmpdir") + "/test_vault_" + System.currentTimeMillis();
    private static final String TEST_CONFIG_FILE = TEST_VAULT_PATH + "/config.enc";
    
    @BeforeEach
    public void setup() throws Exception {
        // Create test vault directory
        Files.createDirectories(Paths.get(TEST_VAULT_PATH));
        
        // Override config file location for testing
        System.setProperty("ghostvault.config.file", TEST_CONFIG_FILE);
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        // Clean up test files
        File configFile = new File(TEST_CONFIG_FILE);
        if (configFile.exists()) {
            configFile.delete();
        }
        
        File vaultDir = new File(TEST_VAULT_PATH);
        if (vaultDir.exists()) {
            vaultDir.delete();
        }
    }
    
    @Test
    public void testCompletePasswordFlow() throws Exception {
        System.out.println("\n=== Testing Complete Password Flow ===\n");
        
        // Step 1: Create passwords
        System.out.println("Step 1: Creating passwords...");
        PasswordManager pm1 = new PasswordManager(TEST_VAULT_PATH);
        
        char[] masterPwd = "TestMaster123!".toCharArray();
        char[] panicPwd = "TestPanic123!".toCharArray();
        char[] decoyPwd = "TestDecoy123!".toCharArray();
        
        pm1.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        System.out.println("✅ Passwords initialized\n");
        
        // Step 2: Verify passwords are configured
        assertTrue(pm1.arePasswordsConfigured(), "Passwords should be configured");
        System.out.println("✅ Passwords are configured\n");
        
        // Step 3: Test password detection with same instance
        System.out.println("Step 3: Testing password detection (same instance)...");
        
        PasswordManager.PasswordType masterType = pm1.detectPassword(masterPwd);
        assertEquals(PasswordManager.PasswordType.MASTER, masterType, "Master password should be detected");
        System.out.println("✅ Master password detected correctly");
        
        PasswordManager.PasswordType panicType = pm1.detectPassword(panicPwd);
        assertEquals(PasswordManager.PasswordType.PANIC, panicType, "Panic password should be detected");
        System.out.println("✅ Panic password detected correctly");
        
        PasswordManager.PasswordType decoyType = pm1.detectPassword(decoyPwd);
        assertEquals(PasswordManager.PasswordType.DECOY, decoyType, "Decoy password should be detected");
        System.out.println("✅ Decoy password detected correctly\n");
        
        // Step 4: Create NEW instance (simulating app restart) and test again
        System.out.println("Step 4: Creating new PasswordManager instance (simulating app restart)...");
        PasswordManager pm2 = new PasswordManager(TEST_VAULT_PATH);
        
        assertTrue(pm2.arePasswordsConfigured(), "Passwords should still be configured after reload");
        System.out.println("✅ Passwords loaded from config file\n");
        
        // Step 5: Test password detection with new instance
        System.out.println("Step 5: Testing password detection (new instance)...");
        
        PasswordManager.PasswordType masterType2 = pm2.detectPassword(masterPwd);
        assertEquals(PasswordManager.PasswordType.MASTER, masterType2, "Master password should be detected after reload");
        System.out.println("✅ Master password detected correctly after reload");
        
        PasswordManager.PasswordType panicType2 = pm2.detectPassword(panicPwd);
        assertEquals(PasswordManager.PasswordType.PANIC, panicType2, "Panic password should be detected after reload");
        System.out.println("✅ Panic password detected correctly after reload");
        
        PasswordManager.PasswordType decoyType2 = pm2.detectPassword(decoyPwd);
        assertEquals(PasswordManager.PasswordType.DECOY, decoyType2, "Decoy password should be detected after reload");
        System.out.println("✅ Decoy password detected correctly after reload\n");
        
        // Step 6: Test invalid password
        System.out.println("Step 6: Testing invalid password...");
        char[] wrongPwd = "WrongPassword123!".toCharArray();
        PasswordManager.PasswordType invalidType = pm2.detectPassword(wrongPwd);
        assertEquals(PasswordManager.PasswordType.INVALID, invalidType, "Wrong password should be invalid");
        System.out.println("✅ Invalid password detected correctly\n");
        
        System.out.println("=== All Password Flow Tests Passed! ===\n");
    }
}
