package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Comprehensive test for PasswordManager functionality
 */
public class PasswordManagerTest {
    
    private static final String TEST_VAULT_PATH = "test-vault";
    
    public static void main(String[] args) {
        PasswordManagerTest test = new PasswordManagerTest();
        
        try {
            test.setUp();
            test.testPasswordStrengthCalculation();
            test.testPasswordInitialization();
            test.testPasswordValidation();
            test.testPasswordTypeDetection();
            test.testSecureStorage();
            test.testPasswordChange();
            test.testConstantTimeComparison();
            test.testSecureDestroy();
            
            System.out.println("✅ All PasswordManager tests passed!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            test.tearDown();
        }
    }
    
    private void setUp() {
        // Clean up any existing test files
        tearDown();
        
        // Create test vault directory
        new File(TEST_VAULT_PATH).mkdirs();
    }
    
    private void tearDown() {
        try {
            // Clean up test files
            Files.deleteIfExists(Paths.get(AppConfig.CONFIG_FILE));
            Files.deleteIfExists(Paths.get(AppConfig.SALT_FILE));
            
            File testDir = new File(TEST_VAULT_PATH);
            if (testDir.exists()) {
                File[] files = testDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                testDir.delete();
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    private void testPasswordStrengthCalculation() {
        System.out.println("Testing password strength calculation...");
        
        // Test various password strengths
        assert PasswordManager.getPasswordStrength("") == 0 : "Empty password should have strength 0";
        assert PasswordManager.getPasswordStrength("weak") == 1 : "Weak password should have low strength";
        assert PasswordManager.getPasswordStrength("Password123!") >= 4 : "Strong password should have high strength";
        
        // Test strength descriptions
        assert "Very Weak".equals(PasswordManager.getPasswordStrengthDescription(1)) : "Strength 1 should be Very Weak";
        assert "Very Strong".equals(PasswordManager.getPasswordStrengthDescription(5)) : "Strength 5 should be Very Strong";
        
        // Test feedback
        String feedback = PasswordManager.getPasswordStrengthFeedback("weak");
        assert feedback.contains("8 characters") : "Feedback should mention length requirement";
        
        System.out.println("✓ Password strength calculation test passed");
    }
    
    private void testPasswordInitialization() throws Exception {
        System.out.println("Testing password initialization...");
        
        PasswordManager passwordManager = new PasswordManager(TEST_VAULT_PATH);
        
        // Initially should not be configured
        assert !passwordManager.arePasswordsConfigured() : "Should not be configured initially";
        
        // Test password initialization
        String masterPwd = "MasterPassword123!";
        String panicPwd = "PanicPassword456@";
        String decoyPwd = "DecoyPassword789#";
        
        passwordManager.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        // Should now be configured
        assert passwordManager.arePasswordsConfigured() : "Should be configured after initialization";
        
        // Test that config file was created
        assert Files.exists(Paths.get(AppConfig.CONFIG_FILE)) : "Config file should exist";
        assert Files.exists(Paths.get(AppConfig.SALT_FILE)) : "Salt file should exist";
        
        System.out.println("✓ Password initialization test passed");
    }
    
    private void testPasswordValidation() throws Exception {
        System.out.println("Testing password validation...");
        
        PasswordManager passwordManager = new PasswordManager(TEST_VAULT_PATH);
        
        String masterPwd = "MasterPassword123!";
        String panicPwd = "PanicPassword456@";
        String decoyPwd = "DecoyPassword789#";
        
        passwordManager.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        // Test correct password validation
        assert passwordManager.validatePassword(masterPwd) == PasswordManager.PasswordType.MASTER : "Master password should validate";
        assert passwordManager.validatePassword(panicPwd) == PasswordManager.PasswordType.PANIC : "Panic password should validate";
        assert passwordManager.validatePassword(decoyPwd) == PasswordManager.PasswordType.DECOY : "Decoy password should validate";
        
        // Test incorrect password
        assert passwordManager.validatePassword("WrongPassword") == PasswordManager.PasswordType.INVALID : "Wrong password should be invalid";
        
        System.out.println("✓ Password validation test passed");
    }
    
    private void testPasswordTypeDetection() throws Exception {
        System.out.println("Testing password type detection...");
        
        PasswordManager passwordManager = new PasswordManager(TEST_VAULT_PATH);
        
        String masterPwd = "MasterPassword123!";
        String panicPwd = "PanicPassword456@";
        String decoyPwd = "DecoyPassword789#";
        
        passwordManager.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        // Test that each password type is correctly identified
        PasswordManager.PasswordType masterType = passwordManager.validatePassword(masterPwd);
        PasswordManager.PasswordType panicType = passwordManager.validatePassword(panicPwd);
        PasswordManager.PasswordType decoyType = passwordManager.validatePassword(decoyPwd);
        
        assert masterType == PasswordManager.PasswordType.MASTER : "Master password type detection failed";
        assert panicType == PasswordManager.PasswordType.PANIC : "Panic password type detection failed";
        assert decoyType == PasswordManager.PasswordType.DECOY : "Decoy password type detection failed";
        
        System.out.println("✓ Password type detection test passed");
    }
    
    private void testSecureStorage() throws Exception {
        System.out.println("Testing secure storage...");
        
        // Create first password manager and initialize
        PasswordManager passwordManager1 = new PasswordManager(TEST_VAULT_PATH);
        
        String masterPwd = "MasterPassword123!";
        String panicPwd = "PanicPassword456@";
        String decoyPwd = "DecoyPassword789#";
        
        passwordManager1.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        // Create second password manager and load from storage
        PasswordManager passwordManager2 = new PasswordManager(TEST_VAULT_PATH);
        
        // Should be able to validate passwords from loaded configuration
        assert passwordManager2.arePasswordsConfigured() : "Second instance should load configuration";
        assert passwordManager2.validatePassword(masterPwd) == PasswordManager.PasswordType.MASTER : "Loaded config should validate master password";
        assert passwordManager2.validatePassword(panicPwd) == PasswordManager.PasswordType.PANIC : "Loaded config should validate panic password";
        assert passwordManager2.validatePassword(decoyPwd) == PasswordManager.PasswordType.DECOY : "Loaded config should validate decoy password";
        
        System.out.println("✓ Secure storage test passed");
    }
    
    private void testPasswordChange() throws Exception {
        System.out.println("Testing password change...");
        
        PasswordManager passwordManager = new PasswordManager(TEST_VAULT_PATH);
        
        String masterPwd = "MasterPassword123!";
        String panicPwd = "PanicPassword456@";
        String decoyPwd = "DecoyPassword789#";
        
        passwordManager.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        // Change master password
        String newMasterPwd = "NewMasterPassword999$";
        passwordManager.changeMasterPassword(masterPwd, newMasterPwd);
        
        // Old password should no longer work
        assert passwordManager.validatePassword(masterPwd) == PasswordManager.PasswordType.INVALID : "Old master password should be invalid";
        
        // New password should work
        assert passwordManager.validatePassword(newMasterPwd) == PasswordManager.PasswordType.MASTER : "New master password should validate";
        
        // Other passwords should still work
        assert passwordManager.validatePassword(panicPwd) == PasswordManager.PasswordType.PANIC : "Panic password should still work";
        assert passwordManager.validatePassword(decoyPwd) == PasswordManager.PasswordType.DECOY : "Decoy password should still work";
        
        System.out.println("✓ Password change test passed");
    }
    
    private void testConstantTimeComparison() throws Exception {
        System.out.println("Testing constant-time comparison...");
        
        PasswordManager passwordManager = new PasswordManager(TEST_VAULT_PATH);
        
        String masterPwd = "MasterPassword123!";
        String panicPwd = "PanicPassword456@";
        String decoyPwd = "DecoyPassword789#";
        
        passwordManager.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        // Test that validation time is consistent regardless of password correctness
        // This is a basic test - in practice, timing analysis would be more sophisticated
        
        long startTime = System.nanoTime();
        passwordManager.validatePassword(masterPwd);
        long correctTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        passwordManager.validatePassword("WrongPassword123!");
        long incorrectTime = System.nanoTime() - startTime;
        
        // Times should be relatively similar (within reasonable bounds)
        // This is a basic check - real timing attack prevention requires more sophisticated analysis
        double ratio = (double) Math.max(correctTime, incorrectTime) / Math.min(correctTime, incorrectTime);
        assert ratio < 10.0 : "Validation times should be relatively consistent to prevent timing attacks";
        
        System.out.println("✓ Constant-time comparison test passed");
    }
    
    private void testSecureDestroy() throws Exception {
        System.out.println("Testing secure destroy...");
        
        PasswordManager passwordManager = new PasswordManager(TEST_VAULT_PATH);
        
        String masterPwd = "MasterPassword123!";
        String panicPwd = "PanicPassword456@";
        String decoyPwd = "DecoyPassword789#";
        
        passwordManager.initializePasswords(masterPwd, panicPwd, decoyPwd);
        
        // Verify passwords work before destroy
        assert passwordManager.validatePassword(masterPwd) == PasswordManager.PasswordType.MASTER : "Master password should work before destroy";
        
        // Perform secure destroy
        passwordManager.secureDestroy();
        
        // Should no longer be configured
        assert !passwordManager.arePasswordsConfigured() : "Should not be configured after destroy";
        
        // All passwords should be invalid
        assert passwordManager.validatePassword(masterPwd) == PasswordManager.PasswordType.INVALID : "Master password should be invalid after destroy";
        assert passwordManager.validatePassword(panicPwd) == PasswordManager.PasswordType.INVALID : "Panic password should be invalid after destroy";
        assert passwordManager.validatePassword(decoyPwd) == PasswordManager.PasswordType.INVALID : "Decoy password should be invalid after destroy";
        
        System.out.println("✓ Secure destroy test passed");
    }
}