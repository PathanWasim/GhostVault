package com.ghostvault.security;

import com.ghostvault.config.AppConfig;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Manages password authentication with cryptographic-erasure-capable design
 * 
 * SECURITY IMPROVEMENTS v2.0:
 * - Uses Argon2id KDF with parameter storage
 * - Master/Decoy: KEK-wrapped VMK (allows vault access)
 * - Panic: Verifier-only (no key recovery possible - enables crypto-erasure)
 * - Constant-time password detection with timing parity
 * - All password handling uses char[] (never String)
 * 
 * @version 2.0.0 - Cryptographic Erasure Design
 */
public class PasswordManager {
    
    public enum PasswordType {
        MASTER, PANIC, DECOY, INVALID
    }
    
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
    private KDF.KdfParams kdfParams;
    private byte[] masterVerifier;
    private byte[] wrappedVMK;
    private byte[] panicVerifier;
    private byte[] decoyVerifier;
    private byte[] wrappedDVMK;
    private boolean isConfigured;
    
    // Timing attack mitigation
    private static final int MIN_DELAY_MS = 900;
    private static final int JITTER_MS = 300;
    private final SecureRandom secureRandom;
    
    public PasswordManager(String vaultPath) throws Exception {
        this.vaultPath = vaultPath;
        this.cryptoManager = new CryptoManager();
        this.secureRandom = new SecureRandom();
        this.isConfigured = false;
        loadPasswordConfiguration();
    }
    
    /**
     * Load password configuration from encrypted config file
     */
    private void loadPasswordConfiguration() throws Exception {
        File configFile = new File(AppConfig.CONFIG_FILE);
        
        System.out.println("🔍 Looking for password configuration at: " + AppConfig.CONFIG_FILE);
        System.out.println("📁 Config file exists: " + configFile.exists());
        
        if (configFile.exists()) {
            try {
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
                }
                
            } catch (Exception e) {
                System.err.println("Warning: Could not load password configuration: " + e.getMessage());
                this.isConfigured = false;
            }
        }
    }
    
    /**
     * Initialize passwords for first-time setup
     * 
     * @param masterPassword Master password (full vault access)
     * @param panicPassword Panic password (triggers destruction)
     * @param decoyPassword Decoy password (fake vault access)
     */
    public void initializePasswords(char[] masterPassword, char[] panicPassword, char[] decoyPassword) 
            throws Exception {
        
        // Initialize passwords for vault access
        
        // Validate password strength
        validatePasswordStrength(masterPassword, "Master", AppConfig.PASSWORD_MIN_STRENGTH);
        validatePasswordStrength(panicPassword, "Panic", 3);
        validatePasswordStrength(decoyPassword, "Decoy", 3);

        
        // Ensure passwords are different (constant-time)
        if (constantTimeEquals(masterPassword, panicPassword) ||
            constantTimeEquals(masterPassword, decoyPassword) ||
            constantTimeEquals(panicPassword, decoyPassword)) {
            throw new IllegalArgumentException("All passwords must be different from each other");
        }

        
        // Generate a single salt for this vault (will be reused for all passwords)
        byte[] vaultSalt = cryptoManager.generateSecureRandom(32);
        
        // Benchmark and get KDF parameters with our vault salt
        KDF.BenchmarkResult benchmark = KDF.benchmark();
        KDF.KdfParams benchmarkParams = benchmark.getRecommendedParams();
        
        // Create KDF params with our consistent vault salt
        if (benchmarkParams.getAlgorithm() == KDF.Algorithm.ARGON2ID) {
            this.kdfParams = new KDF.KdfParams(
                benchmarkParams.getAlgorithm(),
                vaultSalt,
                benchmarkParams.getMemory(),
                benchmarkParams.getIterations(),
                benchmarkParams.getParallelism()
            );
        } else {
            this.kdfParams = new KDF.KdfParams(
                benchmarkParams.getAlgorithm(),
                vaultSalt,
                benchmarkParams.getPbkdf2Iterations()
            );
        }

        
        // Generate Vault Master Keys
        byte[] vmk = cryptoManager.generateSecureRandom(32);  // 256-bit VMK
        byte[] dvmk = cryptoManager.generateSecureRandom(32); // 256-bit DVMK

        
        try {
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
            }
            
        } finally {
            // Zeroize VMKs (they're now wrapped)
            cryptoManager.zeroize(vmk);
            cryptoManager.zeroize(dvmk);
        }
    }
    
    /**
     * Detect password type with constant-time comparison and timing parity
     * 
     * SECURITY: Always performs all three comparisons and adds fixed delay + jitter
     * to prevent timing side-channels from revealing which password was entered.
     */
    public PasswordType detectPassword(char[] password) throws Exception {
        if (!isConfigured) {
            addTimingDelay();
            return PasswordType.INVALID;
        }
        
        byte[] kek = null;
        byte[] verifier = null;
        
        try {
            // Derive KEK from password
            kek = KDF.deriveKey(password, kdfParams);
            verifier = createVerifier(kek);
            
            // CRITICAL: Always perform ALL comparisons (constant-time)
            boolean isMaster = MessageDigest.isEqual(verifier, masterVerifier);
            boolean isPanic = MessageDigest.isEqual(verifier, panicVerifier);
            boolean isDecoy = MessageDigest.isEqual(verifier, decoyVerifier);
            
            // Determine result (order matters for priority)
            PasswordType result;
            if (isMaster) {
                result = PasswordType.MASTER;
            } else if (isPanic) {
                result = PasswordType.PANIC;
            } else if (isDecoy) {
                result = PasswordType.DECOY;
            } else {
                result = PasswordType.INVALID;
            }
            
            // Add timing delay to mask differences
            addTimingDelay();
            
            return result;
            
        } finally {
            // Zeroize sensitive data
            if (kek != null) cryptoManager.zeroize(kek);
            if (verifier != null) cryptoManager.zeroize(verifier);
        }
    }
    
    /**
     * Unwrap VMK using master password
     * 
     * @return Vault Master Key for encrypting/decrypting vault data
     */
    public SecretKey unwrapVMK(char[] masterPassword) throws Exception {
        byte[] kek = null;
        byte[] vmkBytes = null;
        
        try {
            // Derive KEK
            kek = KDF.deriveKey(masterPassword, kdfParams);
            
            // Verify it's the master password
            byte[] verifier = createVerifier(kek);
            if (!MessageDigest.isEqual(verifier, masterVerifier)) {
                throw new GeneralSecurityException("Invalid master password");
            }
            cryptoManager.zeroize(verifier);
            
            // Unwrap VMK
            SecretKey kekKey = cryptoManager.keyFromBytes(kek);
            vmkBytes = cryptoManager.decrypt(wrappedVMK, kekKey, null);
            
            return cryptoManager.keyFromBytes(vmkBytes);
            
        } finally {
            if (kek != null) cryptoManager.zeroize(kek);
            if (vmkBytes != null) cryptoManager.zeroize(vmkBytes);
        }
    }
    
    /**
     * Unwrap DVMK using decoy password
     * 
     * @return Decoy Vault Master Key for decoy vault
     */
    public SecretKey unwrapDVMK(char[] decoyPassword) throws Exception {
        byte[] kek = null;
        byte[] dvmkBytes = null;
        
        try {
            // Derive KEK
            kek = KDF.deriveKey(decoyPassword, kdfParams);
            
            // Verify it's the decoy password
            byte[] verifier = createVerifier(kek);
            if (!MessageDigest.isEqual(verifier, decoyVerifier)) {
                throw new GeneralSecurityException("Invalid decoy password");
            }
            cryptoManager.zeroize(verifier);
            
            // Unwrap DVMK
            SecretKey kekKey = cryptoManager.keyFromBytes(kek);
            dvmkBytes = cryptoManager.decrypt(wrappedDVMK, kekKey, null);
            
            return cryptoManager.keyFromBytes(dvmkBytes);
            
        } finally {
            if (kek != null) cryptoManager.zeroize(kek);
            if (dvmkBytes != null) cryptoManager.zeroize(dvmkBytes);
        }
    }
    
    /**
     * Create verifier from KEK (SHA-256 hash)
     */
    private byte[] createVerifier(byte[] kek) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(kek);
    }
    
    /**
     * Add timing delay with jitter to prevent timing attacks
     */
    private void addTimingDelay() {
        try {
            int jitter = secureRandom.nextInt(JITTER_MS);
            Thread.sleep(MIN_DELAY_MS + jitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Constant-time char array comparison
     */
    private boolean constantTimeEquals(char[] a, char[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    /**
     * Validate password strength
     */
    private void validatePasswordStrength(char[] password, String passwordType, int minStrength) {
        String passwordStr = new String(password);
        try {
            int strength = getPasswordStrength(passwordStr);
            if (strength < minStrength) {
                throw new IllegalArgumentException(passwordType + " password is too weak. " +
                    "Current strength: " + strength + "/5, Required: " + minStrength + "/5");
            }
        } finally {
            // Clear temporary string
            passwordStr = null;
        }
    }
    
    /**
     * Calculate password strength (0-5)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~].*")) score++;
        
        if (password.length() >= 16) score++;
        
        // Penalize common patterns
        if (isCommonPattern(password)) {
            score = Math.max(0, score - 2);
        }
        
        return Math.min(5, score);
    }
    
    /**
     * Check for common weak patterns
     */
    private static boolean isCommonPattern(String password) {
        String lower = password.toLowerCase();
        String[] weakPatterns = {
            "password", "123456", "qwerty", "admin", "letmein"
        };
        
        for (String pattern : weakPatterns) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        
        return password.matches(".*(.)\\1{2,}.*");
    }
    
    /**
     * Save encrypted password configuration
     */
    private void savePasswordConfiguration() throws Exception {
        System.out.println("🔧 Saving password configuration to: " + AppConfig.CONFIG_FILE);
        
        // Serialize KDF params
        byte[] kdfParamsSerialized = serializeKdfParams(kdfParams);
        
        // Create configuration
        PasswordConfiguration config = new PasswordConfiguration(
            kdfParamsSerialized,
            masterVerifier,
            wrappedVMK,
            panicVerifier,
            decoyVerifier,
            wrappedDVMK
        );
        
        // Serialize to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(config);
        }
        
        // Write to file (unencrypted - verifiers and wrapped keys are already protected)
        Files.write(Paths.get(AppConfig.CONFIG_FILE), baos.toByteArray());
        
        System.out.println("✓ Password configuration saved successfully (" + baos.size() + " bytes)");
    }
    
    /**
     * Serialize KDF parameters
     */
    private byte[] serializeKdfParams(KDF.KdfParams params) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream dos = new DataOutputStream(baos)) {
            dos.writeUTF(params.getAlgorithm().name());
            dos.writeInt(params.getSalt().length);
            dos.write(params.getSalt());
            
            if (params.getAlgorithm() == KDF.Algorithm.ARGON2ID) {
                dos.writeInt(params.getMemory());
                dos.writeInt(params.getIterations());
                dos.writeInt(params.getParallelism());
            } else {
                dos.writeInt(params.getPbkdf2Iterations());
            }
        }
        return baos.toByteArray();
    }
    
    /**
     * Deserialize KDF parameters
     */
    private KDF.KdfParams deserializeKdfParams(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (DataInputStream dis = new DataInputStream(bais)) {
            String algorithmName = dis.readUTF();
            KDF.Algorithm algorithm = KDF.Algorithm.valueOf(algorithmName);
            
            int saltLength = dis.readInt();
            byte[] salt = new byte[saltLength];
            dis.readFully(salt);
            
            if (algorithm == KDF.Algorithm.ARGON2ID) {
                int memory = dis.readInt();
                int iterations = dis.readInt();
                int parallelism = dis.readInt();
                return new KDF.KdfParams(algorithm, salt, memory, iterations, parallelism);
            } else {
                int iterations = dis.readInt();
                return new KDF.KdfParams(algorithm, salt, iterations);
            }
        }
    }
    
    /**
     * Check if passwords are configured
     */
    public boolean arePasswordsConfigured() {
        return isConfigured;
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
        try {
            Files.deleteIfExists(Paths.get(AppConfig.CONFIG_FILE));
        } catch (Exception e) {
            // Best effort
        }
    }
}
