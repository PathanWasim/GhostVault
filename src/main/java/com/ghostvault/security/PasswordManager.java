package com.ghostvault.security;

import com.ghostvault.config.AppConfig;
<<<<<<< HEAD
import com.ghostvault.config.ConfigurationValidator;
=======
import com.ghostvault.ui.controllers.ModeController;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
>>>>>>> 5e3dbee5708a73a7823118329611adf0497308f8

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
    
<<<<<<< HEAD
    /**
     * Password configuration with KEK wrapping
     */
    public static class PasswordConfiguration implements Serializable {
        private static final long serialVersionUID = 2L;
        
        // KDF parameters
        private final byte[] kdfParamsSerialized;
        
        // Master password: KEK-wrapped VMK
        private final byte[] masterVerifier;  // For detection
        private final byte[] wrappedVMK;      // Encrypted VMK
        
        // Panic password: Verifier only (NO key recovery)
        private final byte[] panicVerifier;
        
        // Decoy password: KEK-wrapped DVMK
        private final byte[] decoyVerifier;
        private final byte[] wrappedDVMK;
        
        public PasswordConfiguration(byte[] kdfParams, 
                                   byte[] masterVerifier, byte[] wrappedVMK,
                                   byte[] panicVerifier,
                                   byte[] decoyVerifier, byte[] wrappedDVMK) {
            this.kdfParamsSerialized = kdfParams.clone();
            this.masterVerifier = masterVerifier.clone();
            this.wrappedVMK = wrappedVMK.clone();
            this.panicVerifier = panicVerifier.clone();
            this.decoyVerifier = decoyVerifier.clone();
            this.wrappedDVMK = wrappedDVMK.clone();
        }
        
        public byte[] getKdfParams() { return kdfParamsSerialized.clone(); }
        public byte[] getMasterVerifier() { return masterVerifier.clone(); }
        public byte[] getWrappedVMK() { return wrappedVMK.clone(); }
        public byte[] getPanicVerifier() { return panicVerifier.clone(); }
        public byte[] getDecoyVerifier() { return decoyVerifier.clone(); }
        public byte[] getWrappedDVMK() { return wrappedDVMK.clone(); }
    }
    
    private final String vaultPath;
    private final CryptoManager cryptoManager;
    private final ConfigurationValidator configValidator;
    private KDF.KdfParams kdfParams;
    private byte[] masterVerifier;
    private byte[] wrappedVMK;
    private byte[] panicVerifier;
    private byte[] decoyVerifier;
    private byte[] wrappedDVMK;
    private boolean isConfigured;
    private ConfigurationValidator.ValidationResult lastValidationResult;
    
    // Timing attack mitigation
    private static final int MIN_DELAY_MS = 900;
    private static final int JITTER_MS = 300;
    private final SecureRandom secureRandom;
    
    public PasswordManager(String vaultPath) throws Exception {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        this.configValidator = new ConfigurationValidator();
        this.secureRandom = new SecureRandom();
        this.isConfigured = false;
        loadPasswordConfiguration();
    }
    
    /**
     * Load password configuration from encrypted config file with enhanced validation
     */
    private void loadPasswordConfiguration() throws Exception {
        System.out.println("🔍 Loading password configuration with enhanced validation");
        
        // Validate configuration using ConfigurationValidator
        lastValidationResult = configValidator.validateConfiguration();
        
        System.out.println("📋 Configuration validation result: " + lastValidationResult.getStatus());
        
        switch (lastValidationResult.getStatus()) {
            case VALID:
                loadValidConfiguration();
                break;
                
            case BACKUP_AVAILABLE:
                System.out.println("⚠️ Primary config corrupted, attempting backup recovery");
                if (configValidator.restoreFromBackup()) {
                    System.out.println("✅ Configuration restored from backup");
                    loadValidConfiguration();
                } else {
                    handleConfigurationError("Failed to restore from backup");
                }
                break;
                
            case CORRUPTED:
                System.err.println("❌ Configuration file is corrupted");
                if (configValidator.performRecovery()) {
                    System.out.println("✅ Configuration recovered");
                    loadValidConfiguration();
                } else {
                    handleConfigurationError("Configuration corrupted and cannot be recovered");
                }
                break;
                
            case INCOMPLETE:
                System.err.println("⚠️ Configuration is incomplete");
                if (configValidator.performRecovery()) {
                    System.out.println("✅ Configuration completed from backup");
                    loadValidConfiguration();
                } else {
                    handleConfigurationError("Configuration incomplete and cannot be recovered");
                }
                break;
                
            case MISSING:
                System.out.println("📝 No configuration found - first run setup required");
                this.isConfigured = false;
                break;
                
            case INACCESSIBLE:
                handleConfigurationError("Configuration file cannot be accessed: " + 
                    lastValidationResult.getErrorMessage());
                break;
                
            default:
                handleConfigurationError("Unknown configuration status: " + 
                    lastValidationResult.getStatus());
                break;
        }
    }
    
    /**
     * Load valid configuration file
     */
    private void loadValidConfiguration() throws Exception {
        try {
            File configFile = new File(AppConfig.CONFIG_FILE);
            byte[] configData = Files.readAllBytes(configFile.toPath());
            
            // Deserialize configuration
            ByteArrayInputStream bais = new ByteArrayInputStream(configData);
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                PasswordConfiguration config = (PasswordConfiguration) ois.readObject();
                
                // Deserialize KDF params
                this.kdfParams = deserializeKdfParams(config.getKdfParams());
                
                // Load verifiers and wrapped keys
                this.masterVerifier = config.getMasterVerifier();
                this.wrappedVMK = config.getWrappedVMK();
                this.panicVerifier = config.getPanicVerifier();
                this.decoyVerifier = config.getDecoyVerifier();
                this.wrappedDVMK = config.getWrappedDVMK();
                
                this.isConfigured = true;
                
                System.out.println("✅ Password configuration loaded successfully");
=======
    private void loadConfig() {
        config = new Properties();
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                config.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load vault configuration", e);
>>>>>>> 5e3dbee5708a73a7823118329611adf0497308f8
            }
            
        } catch (Exception e) {
            throw new Exception("Failed to load valid configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle configuration errors with appropriate logging and recovery
     */
    private void handleConfigurationError(String errorMessage) {
        System.err.println("❌ Configuration Error: " + errorMessage);
        this.isConfigured = false;
        
        // Log detailed error information
        if (lastValidationResult != null) {
            System.err.println("📊 Validation Details:");
            System.err.println("   Status: " + lastValidationResult.getStatus());
            System.err.println("   Error: " + lastValidationResult.getErrorMessage());
            System.err.println("   Recovery Action: " + lastValidationResult.getRecoveryAction());
            System.err.println("   Can Recover: " + lastValidationResult.canRecover());
        }
    }
    
    private void saveConfig() {
        try {
<<<<<<< HEAD
            // Derive KEKs from passwords
            byte[] masterKEK = KDF.deriveKey(masterPassword, kdfParams);
            byte[] panicKEK = KDF.deriveKey(panicPassword, kdfParams);
            byte[] decoyKEK = KDF.deriveKey(decoyPassword, kdfParams);
            
            try {
                // Create verifiers (hash of KEK for constant-time comparison)
                this.masterVerifier = createVerifier(masterKEK);
                this.panicVerifier = createVerifier(panicKEK);
                this.decoyVerifier = createVerifier(decoyKEK);
                
                // Wrap VMK and DVMK with KEKs
                SecretKey masterKey = cryptoManager.keyFromBytes(masterKEK);
                SecretKey decoyKey = cryptoManager.keyFromBytes(decoyKEK);
                
                this.wrappedVMK = cryptoManager.encrypt(vmk, masterKey, null);
                this.wrappedDVMK = cryptoManager.encrypt(dvmk, decoyKey, null);
                
                // Save configuration
                savePasswordConfiguration();
                this.isConfigured = true;
                
                // Create backup and checksum after successful initialization
                try {
                    configValidator.createBackup();
                    configValidator.createChecksum(Paths.get(AppConfig.CONFIG_FILE));
                    System.out.println("✅ Configuration backup and checksum created");
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to create configuration backup: " + e.getMessage());
                }
                
                // Verify password detection works
                PasswordType testResult = detectPassword(masterPassword);
                if (testResult != PasswordType.MASTER) {
                    throw new Exception("Password verification failed");
                }
                
            } finally {
                // Zeroize KEKs
                cryptoManager.zeroize(masterKEK);
                cryptoManager.zeroize(panicKEK);
                cryptoManager.zeroize(decoyKEK);
=======
            Files.createDirectories(configPath.getParent());
            try (OutputStream output = Files.newOutputStream(configPath)) {
                config.store(output, "GhostVault Configuration - DO NOT EDIT MANUALLY");
>>>>>>> 5e3dbee5708a73a7823118329611adf0497308f8
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
    
<<<<<<< HEAD
    /**
     * Check if passwords are configured with enhanced validation
     */
    public boolean arePasswordsConfigured() {
        // If we haven't validated recently, re-validate
        if (lastValidationResult == null) {
            try {
                lastValidationResult = configValidator.validateConfiguration();
            } catch (Exception e) {
                System.err.println("Configuration validation failed: " + e.getMessage());
                return false;
            }
        }
        
        // Return true only if configuration is valid and we're marked as configured
        return isConfigured && lastValidationResult.isValid();
    }
    
    /**
     * Get configuration validation result
     */
    public ConfigurationValidator.ValidationResult getConfigurationValidation() {
        if (lastValidationResult == null) {
            lastValidationResult = configValidator.validateConfiguration();
        }
        return lastValidationResult;
    }
    
    /**
     * Get configuration status
     */
    public ConfigurationValidator.ConfigurationStatus getConfigurationStatus() {
        return getConfigurationValidation().getStatus();
    }
    
    /**
     * Get detailed configuration information
     */
    public String getConfigurationInfo() {
        return configValidator.getConfigurationInfo();
    }
    
    /**
     * Attempt to recover configuration
     */
    public boolean recoverConfiguration() {
        boolean recovered = configValidator.performRecovery();
        if (recovered) {
            try {
                // Reload configuration after recovery
                loadPasswordConfiguration();
                return isConfigured;
            } catch (Exception e) {
                System.err.println("Failed to reload configuration after recovery: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    /**
     * Get KDF parameters (for metadata storage)
     */
    public KDF.KdfParams getKdfParams() {
        return kdfParams;
    }
    
    /**
     * Get password strength description (for UI)
     */
    public static String getPasswordStrengthDescription(int score) {
        switch (score) {
            case 0: return "";
            case 1: return "Very Weak";
            case 2: return "Weak";
            case 3: return "Fair";
            case 4: return "Strong";
            case 5: return "Very Strong";
            default: return "Unknown";
        }
    }
    
    /**
     * Get password strength color (for UI)
     */
    public static String getPasswordStrengthColor(int score) {
        switch (score) {
            case 1: return "#f44336"; // Red
            case 2: return "#ff9800"; // Orange
            case 3: return "#ffeb3b"; // Yellow
            case 4: return "#8bc34a"; // Light Green
            case 5: return "#4caf50"; // Green
            default: return "#cccccc"; // Gray
        }
    }
    
    /**
     * Get password strength feedback (for UI)
     */
    public static String getPasswordStrengthFeedback(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        
        StringBuilder feedback = new StringBuilder();
        
        if (password.length() < 8) {
            feedback.append("Use at least 8 characters. ");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            feedback.append("Add uppercase letters. ");
        }
        
        if (!password.matches(".*[a-z].*")) {
            feedback.append("Add lowercase letters. ");
        }
        
        if (!password.matches(".*\\d.*")) {
            feedback.append("Add numbers. ");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~].*")) {
            feedback.append("Add special characters. ");
        }
        
        if (isCommonPattern(password)) {
            feedback.append("Avoid common words and patterns. ");
        }
        
        if (feedback.length() == 0) {
            return "Strong password!";
        }
        
        return feedback.toString().trim();
    }
    
    /**
     * Securely destroy all password data (for panic mode)
     * This implements cryptographic erasure - without the wrapped keys,
     * the vault data becomes permanently unrecoverable.
     */
    public void secureDestroy() {
        // Zeroize all sensitive data
        if (masterVerifier != null) cryptoManager.zeroize(masterVerifier);
        if (wrappedVMK != null) cryptoManager.zeroize(wrappedVMK);
        if (panicVerifier != null) cryptoManager.zeroize(panicVerifier);
        if (decoyVerifier != null) cryptoManager.zeroize(decoyVerifier);
        if (wrappedDVMK != null) cryptoManager.zeroize(wrappedDVMK);
        
        masterVerifier = null;
        wrappedVMK = null;
        panicVerifier = null;
        decoyVerifier = null;
        wrappedDVMK = null;
        kdfParams = null;
        isConfigured = false;
        
        // Clear crypto manager
        cryptoManager.clearKeys();
        
        // Delete config file
=======
    private void createDecoyFiles(Path decoyDir) {
>>>>>>> 5e3dbee5708a73a7823118329611adf0497308f8
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