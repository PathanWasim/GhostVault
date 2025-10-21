package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
import com.ghostvault.ui.controllers.ModeController;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

/**
 * Manages secure password storage and authentication for the three-password system
 */
public class PasswordManager {
    
    private static final String CONFIG_FILE = "vault.config";
    private static final String MASTER_KEY = "master.hash";
    private static final String PANIC_KEY = "panic.hash";
    private static final String DECOY_KEY = "decoy.hash";
    private static final String SALT_KEY = "salt";
    private static final String SETUP_COMPLETE_KEY = "setup.complete";
    
    private final Argon2 argon2;
    private final Path configPath;
    private Properties config;
    
    public PasswordManager() {
        this.argon2 = Argon2Factory.create();
        this.configPath = Paths.get(AppConfig.VAULT_DIR, CONFIG_FILE);
        loadConfig();
    }
    
    private void loadConfig() {
        config = new Properties();
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                config.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load vault configuration", e);
            }
        }
    }
    
    private void saveConfig() {
        try {
            Files.createDirectories(configPath.getParent());
            try (OutputStream output = Files.newOutputStream(configPath)) {
                config.store(output, "GhostVault Configuration - DO NOT EDIT MANUALLY");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save vault configuration", e);
        }
    }
    
    public boolean isSetupComplete() {
        return "true".equals(config.getProperty(SETUP_COMPLETE_KEY, "false"));
    }
    
    public void setMasterPassword(String password) {
        String salt = getOrCreateSalt();
        String hash = hashPassword(password, salt);
        config.setProperty(MASTER_KEY, hash);
        saveConfig();
    }
    
    public void setPanicPassword(String password) {
        String salt = getOrCreateSalt();
        String hash = hashPassword(password, salt);
        config.setProperty(PANIC_KEY, hash);
        saveConfig();
    }
    
    public void setDecoyPassword(String password) {
        String salt = getOrCreateSalt();
        String hash = hashPassword(password, salt);
        config.setProperty(DECOY_KEY, hash);
        saveConfig();
    }
    
    public void markSetupComplete() {
        config.setProperty(SETUP_COMPLETE_KEY, "true");
        saveConfig();
    }
    
    public ModeController.VaultMode authenticatePassword(String password) {
        if (!isSetupComplete()) {
            throw new IllegalStateException("Vault setup is not complete");
        }
        
        String salt = config.getProperty(SALT_KEY);
        if (salt == null) {
            throw new IllegalStateException("Vault configuration is corrupted");
        }
        
        // Check against Master password
        String masterHash = config.getProperty(MASTER_KEY);
        if (masterHash != null && verifyPassword(password, masterHash, salt)) {
            return ModeController.VaultMode.MASTER;
        }
        
        // Check against Panic password
        String panicHash = config.getProperty(PANIC_KEY);
        if (panicHash != null && verifyPassword(password, panicHash, salt)) {
            return ModeController.VaultMode.PANIC;
        }
        
        // Check against Decoy password
        String decoyHash = config.getProperty(DECOY_KEY);
        if (decoyHash != null && verifyPassword(password, decoyHash, salt)) {
            return ModeController.VaultMode.DECOY;
        }
        
        // No password matched
        return null;
    }
    
    private String getOrCreateSalt() {
        String salt = config.getProperty(SALT_KEY);
        if (salt == null) {
            salt = generateSalt();
            config.setProperty(SALT_KEY, salt);
            saveConfig();
        }
        return salt;
    }
    
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[32];
        random.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }
    
    private String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            String hash = argon2.hash(10, 65536, 1, password, java.nio.charset.StandardCharsets.UTF_8);
            return hash;
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
    
    private boolean verifyPassword(String password, String hash, String salt) {
        try {
            return argon2.verify(hash, password, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return false;
        }
    }
    
    public void initializeVaultDirectories() {
        try {
            // Create main vault directory
            Path vaultDir = Paths.get(AppConfig.VAULT_DIR);
            Files.createDirectories(vaultDir);
            
            // Create real vault directory (Master mode)
            Path realVaultDir = Paths.get(AppConfig.VAULT_DIR, "real");
            Files.createDirectories(realVaultDir);
            
            // Create decoy vault directory with some fake files
            Path decoyVaultDir = Paths.get(AppConfig.VAULT_DIR, "decoy");
            Files.createDirectories(decoyVaultDir);
            createDecoyFiles(decoyVaultDir);
            
            // Mark setup as complete
            markSetupComplete();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize vault directories", e);
        }
    }
    
    private void createDecoyFiles(Path decoyDir) {
        try {
            // Create some believable decoy files
            createDecoyFile(decoyDir, "personal_notes.txt", 
                "Meeting notes from today:\n- Discussed project timeline\n- Need to follow up on budget approval\n- Schedule team meeting for next week");
            
            createDecoyFile(decoyDir, "shopping_list.txt", 
                "Grocery List:\n- Milk\n- Bread\n- Eggs\n- Apples\n- Chicken\n- Rice");
            
            createDecoyFile(decoyDir, "vacation_photos", null); // Directory
            
            createDecoyFile(decoyDir, "work_documents", null); // Directory
            
            // Create subdirectories with files
            Path vacationDir = decoyDir.resolve("vacation_photos");
            Files.createDirectories(vacationDir);
            createDecoyFile(vacationDir, "beach_sunset.jpg.txt", "Placeholder for beach sunset photo");
            createDecoyFile(vacationDir, "family_dinner.jpg.txt", "Placeholder for family dinner photo");
            
            Path workDir = decoyDir.resolve("work_documents");
            Files.createDirectories(workDir);
            createDecoyFile(workDir, "quarterly_report.docx.txt", "Q3 Financial Summary\nRevenue: $125,000\nExpenses: $98,000\nProfit: $27,000");
            createDecoyFile(workDir, "presentation.pptx.txt", "Project Presentation Outline\n1. Introduction\n2. Current Status\n3. Next Steps\n4. Q&A");
            
        } catch (IOException e) {
            System.err.println("Warning: Failed to create some decoy files: " + e.getMessage());
        }
    }
    
    private void createDecoyFile(Path parentDir, String filename, String content) throws IOException {
        if (content == null) {
            // Create directory
            Files.createDirectories(parentDir.resolve(filename));
        } else {
            // Create file with content
            Path filePath = parentDir.resolve(filename);
            Files.write(filePath, content.getBytes());
        }
    }
    
    public Path getVaultDirectory(ModeController.VaultMode mode) {
        switch (mode) {
            case MASTER:
                return Paths.get(AppConfig.VAULT_DIR, "real");
            case DECOY:
                return Paths.get(AppConfig.VAULT_DIR, "decoy");
            case PANIC:
                // Panic mode shows real directory initially, but operations trigger wipe
                return Paths.get(AppConfig.VAULT_DIR, "real");
            default:
                throw new IllegalArgumentException("Unknown vault mode: " + mode);
        }
    }
    
    public void cleanup() {
        if (argon2 != null) {
            // Clear any sensitive data from memory
            System.gc();
        }
    }
}