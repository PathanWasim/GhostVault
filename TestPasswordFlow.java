import com.ghostvault.security.PasswordManager;
import com.ghostvault.config.AppConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Standalone test to verify password flow
 * Compile: javac -cp "target/classes;..." TestPasswordFlow.java
 * Run: java -cp "target/classes;...;." TestPasswordFlow
 */
public class TestPasswordFlow {
    
    public static void main(String[] args) {
        System.out.println("\n=== Password Flow Test ===\n");
        
        try {
            // Use a test vault path
            String testVaultPath = System.getProperty("java.io.tmpdir") + "/test_vault_" + System.currentTimeMillis();
            Files.createDirectories(Paths.get(testVaultPath));
            
            System.out.println("Test vault: " + testVaultPath);
            
            // Step 1: Create PasswordManager and initialize passwords
            System.out.println("\n1. Creating PasswordManager and initializing passwords...");
            PasswordManager pm = new PasswordManager(testVaultPath);
            
            char[] masterPwd = "TestMaster123!".toCharArray();
            char[] panicPwd = "TestPanic123!".toCharArray();
            char[] decoyPwd = "TestDecoy123!".toCharArray();
            
            pm.initializePasswords(masterPwd, panicPwd, decoyPwd);
            
            System.out.println("✅ Passwords initialized");
            
            // Step 2: Test password detection
            System.out.println("\n2. Testing password detection...");
            
            PasswordManager.PasswordType masterType = pm.detectPassword(masterPwd);
            System.out.println("Master password detected as: " + masterType);
            
            if (masterType == PasswordManager.PasswordType.MASTER) {
                System.out.println("✅ Master password works!");
            } else {
                System.out.println("❌ Master password FAILED!");
            }
            
            // Step 3: Create NEW instance (simulating app restart)
            System.out.println("\n3. Creating new PasswordManager instance (simulating restart)...");
            PasswordManager pm2 = new PasswordManager(testVaultPath);
            
            System.out.println("Passwords configured: " + pm2.arePasswordsConfigured());
            
            // Step 4: Test password detection with new instance
            System.out.println("\n4. Testing password detection after reload...");
            
            PasswordManager.PasswordType masterType2 = pm2.detectPassword(masterPwd);
            System.out.println("Master password detected as: " + masterType2);
            
            if (masterType2 == PasswordManager.PasswordType.MASTER) {
                System.out.println("✅ Master password works after reload!");
            } else {
                System.out.println("❌ Master password FAILED after reload!");
            }
            
            // Cleanup
            new File(testVaultPath + "/config.enc").delete();
            new File(testVaultPath).delete();
            
            System.out.println("\n=== Test Complete ===\n");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed with exception:");
            e.printStackTrace();
        }
    }
}
